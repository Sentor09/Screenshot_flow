package com.flowsnap.canvas

import kotlin.math.max
import kotlin.math.min

/** A location in the flow document, independent of device pixels and density. */
data class CanvasPoint(val x: Float, val y: Float) {
    operator fun plus(other: CanvasPoint) = CanvasPoint(x + other.x, y + other.y)
    operator fun minus(other: CanvasPoint) = CanvasPoint(x - other.x, y - other.y)
}

data class CanvasSize(val width: Float, val height: Float) {
    init {
        require(width >= 0f) { "width must not be negative" }
        require(height >= 0f) { "height must not be negative" }
    }
}

/** Axis-aligned bounds in logical canvas coordinates. */
data class CanvasBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    init {
        require(right >= left) { "right must be greater than or equal to left" }
        require(bottom >= top) { "bottom must be greater than or equal to top" }
    }

    val width get() = right - left
    val height get() = bottom - top
    val center get() = CanvasPoint(left + width / 2f, top + height / 2f)

    fun contains(point: CanvasPoint): Boolean =
        point.x in left..right && point.y in top..bottom

    fun expandedBy(amount: Float): CanvasBounds {
        require(amount >= 0f) { "amount must not be negative" }
        return CanvasBounds(left - amount, top - amount, right + amount, bottom + amount)
    }

    companion object {
        fun enclosing(bounds: Iterable<CanvasBounds>): CanvasBounds? {
            val iterator = bounds.iterator()
            if (!iterator.hasNext()) return null
            var result = iterator.next()
            while (iterator.hasNext()) {
                val next = iterator.next()
                result = CanvasBounds(
                    left = min(result.left, next.left),
                    top = min(result.top, next.top),
                    right = max(result.right, next.right),
                    bottom = max(result.bottom, next.bottom)
                )
            }
            return result
        }
    }
}

/** Persisted node geometry. Positions identify the top-left corner of a screenshot node. */
data class CanvasNode(
    val id: String,
    val position: CanvasPoint,
    val size: CanvasSize
) {
    val bounds get() = CanvasBounds(position.x, position.y, position.x + size.width, position.y + size.height)
}

/** The deterministic layout assigned to captures before a user moves nodes. */
object InitialCanvasLayout {
    const val COLUMNS = 5
    const val COLUMN_SPACING = 400f
    const val ROW_SPACING = 760f

    fun positionFor(screenPosition: Int, columns: Int = COLUMNS): CanvasPoint {
        require(screenPosition >= 0) { "screenPosition must not be negative" }
        val resolvedColumns = columns.coerceAtLeast(1)
        return CanvasPoint(
            x = (screenPosition % resolvedColumns) * COLUMN_SPACING,
            y = (screenPosition / resolvedColumns) * ROW_SPACING
        )
    }
}

/** Produces a deterministic left-to-right layout while retaining a stable fallback for cycles. */
fun autoLayoutCanvas(nodeIdsInOrder: List<String>, edges: Iterable<CanvasEdge>): Map<String, CanvasPoint> {
    val nodeIds = nodeIdsInOrder.toSet()
    val children = nodeIds.associateWith { mutableListOf<String>() }
    val incoming = nodeIds.associateWith { 0 }.toMutableMap()
    edges.forEach { edge ->
        if (edge.fromNodeId in nodeIds && edge.toNodeId in nodeIds && edge.fromNodeId != edge.toNodeId) {
            children.getValue(edge.fromNodeId) += edge.toNodeId
            incoming[edge.toNodeId] = incoming.getValue(edge.toNodeId) + 1
        }
    }
    val levels = mutableMapOf<String, Int>()
    val roots = nodeIdsInOrder.filter { incoming.getValue(it) == 0 }
    roots.forEach { levels[it] = 0 }
    val ready = ArrayDeque(roots)
    while (ready.isNotEmpty()) {
        val node = ready.removeFirst()
        val level = levels[node] ?: 0
        children.getValue(node).forEach { child ->
            levels[child] = max(levels[child] ?: 0, level + 1)
            incoming[child] = incoming.getValue(child) - 1
            if (incoming.getValue(child) == 0) ready += child
        }
    }
    // A cycle has no root. Place its members after the acyclic graph in capture order.
    val fallbackLevel = (levels.values.maxOrNull() ?: -1) + 1
    nodeIdsInOrder.filterNot { it in levels }.forEachIndexed { index, id -> levels[id] = fallbackLevel + index }
    return nodeIdsInOrder.groupBy { levels[it] ?: 0 }.flatMap { (level, ids) ->
        ids.mapIndexed { row, id -> id to CanvasPoint(level * InitialCanvasLayout.COLUMN_SPACING, row * InitialCanvasLayout.ROW_SPACING) }
    }.toMap()
}

/** A directed graph edge. This intentionally contains no presentation metadata. */
data class CanvasEdge(val fromNodeId: String, val toNodeId: String)

data class CanvasEndpoints(val startNodeIds: Set<String>, val endNodeIds: Set<String>)

/** Derives roots and leaves from valid directed edges; isolated nodes are both. */
fun deriveCanvasEndpoints(nodeIds: Set<String>, edges: Iterable<CanvasEdge>): CanvasEndpoints {
    val incoming = mutableSetOf<String>()
    val outgoing = mutableSetOf<String>()
    edges.forEach { edge ->
        if (edge.fromNodeId in nodeIds && edge.toNodeId in nodeIds) {
            outgoing += edge.fromNodeId
            incoming += edge.toNodeId
        }
    }
    return CanvasEndpoints(
        startNodeIds = nodeIds - incoming,
        endNodeIds = nodeIds - outgoing
    )
}

/** Maps between device pixels and the flow's logical, persisted canvas coordinates. */
data class CanvasViewport(
    val zoom: Float = 1f,
    val pan: CanvasPoint = CanvasPoint(0f, 0f)
) {
    init {
        require(zoom > 0f) { "zoom must be positive" }
    }

    fun toScreen(point: CanvasPoint): CanvasPoint = CanvasPoint(
        x = point.x * zoom + pan.x,
        y = point.y * zoom + pan.y
    )

    fun toCanvas(point: CanvasPoint): CanvasPoint = CanvasPoint(
        x = (point.x - pan.x) / zoom,
        y = (point.y - pan.y) / zoom
    )

    fun pannedBy(screenDelta: CanvasPoint): CanvasViewport = copy(pan = pan + screenDelta)

    fun zoomedAround(focalScreenPoint: CanvasPoint, targetZoom: Float): CanvasViewport {
        require(targetZoom > 0f) { "targetZoom must be positive" }
        val focalCanvasPoint = toCanvas(focalScreenPoint)
        return CanvasViewport(
            zoom = targetZoom,
            pan = CanvasPoint(
                focalScreenPoint.x - focalCanvasPoint.x * targetZoom,
                focalScreenPoint.y - focalCanvasPoint.y * targetZoom
            )
        )
    }

    companion object {
        fun fit(bounds: CanvasBounds, viewportSize: CanvasSize, padding: Float, minZoom: Float, maxZoom: Float): CanvasViewport {
            require(padding >= 0f) { "padding must not be negative" }
            require(minZoom > 0f && maxZoom >= minZoom) { "invalid zoom range" }
            val availableWidth = (viewportSize.width - padding * 2f).coerceAtLeast(1f)
            val availableHeight = (viewportSize.height - padding * 2f).coerceAtLeast(1f)
            val scaleX = availableWidth / bounds.width.coerceAtLeast(1f)
            val scaleY = availableHeight / bounds.height.coerceAtLeast(1f)
            val zoom = min(scaleX, scaleY).coerceIn(minZoom, maxZoom)
            val centeredPan = CanvasPoint(
                viewportSize.width / 2f - bounds.center.x * zoom,
                viewportSize.height / 2f - bounds.center.y * zoom
            )
            return CanvasViewport(zoom, centeredPan)
        }
    }
}

/** Returns the topmost node containing a screen-space tap, or null for empty canvas. */
fun hitTestCanvasNodes(screenPoint: CanvasPoint, viewport: CanvasViewport, nodesInDrawOrder: List<CanvasNode>): CanvasNode? {
    val canvasPoint = viewport.toCanvas(screenPoint)
    return nodesInDrawOrder.asReversed().firstOrNull { it.bounds.contains(canvasPoint) }
}

/** Finds the point on a node boundary closest to an incoming connector. */
fun nearestPointOnCanvasBounds(point: CanvasPoint, bounds: CanvasBounds): CanvasPoint {
    val clampedX = point.x.coerceIn(bounds.left, bounds.right)
    val clampedY = point.y.coerceIn(bounds.top, bounds.bottom)
    if (clampedX != point.x || clampedY != point.y) return CanvasPoint(clampedX, clampedY)
    val distances = listOf(
        point.x - bounds.left to CanvasPoint(bounds.left, point.y),
        bounds.right - point.x to CanvasPoint(bounds.right, point.y),
        point.y - bounds.top to CanvasPoint(point.x, bounds.top),
        bounds.bottom - point.y to CanvasPoint(point.x, bounds.bottom)
    )
    return distances.minBy { it.first }.second
}
