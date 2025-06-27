package com.example.nanoboltron.jsonschema.analyzer

import android.util.Log
import com.example.nanoboltron.jsonschema.parser.NodeTraverser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

/**
 * Specialized component for analyzing JSON Schema descriptor trees.
 * Provides comprehensive schema analysis, statistics, and insights.
 */
class SchemaAnalyzer {

    private val nodeTraverser = NodeTraverser()

    /**
     * Data class containing comprehensive schema analysis results
     */
    data class SchemaAnalysisResult(
        val schemaName: String,
        val totalNodes: Int,
        val formFields: List<NodeTraverser.NodePath>,
        val compositionNodes: List<NodeTraverser.NodePath>,
        val conditionalNodes: List<NodeTraverser.NodePath>,
        val allPaths: List<NodeTraverser.NodePath>,
        val maxDepth: Int,
        val treeStructure: String
    )

    /**
     * Performs comprehensive analysis of a schema and returns structured results
     */
    fun analyzeSchema(rootNode: DescriptorNode, schemaName: String): SchemaAnalysisResult {
        // Get comprehensive schema analysis
        val allPaths = nodeTraverser.getAllPaths(rootNode)
        val formFields = nodeTraverser.getLeafNodes(rootNode)
        val compositionNodes = nodeTraverser.getCompositionNodes(rootNode)
        val conditionalNodes = nodeTraverser.getConditionalNodes(rootNode)
        val maxDepth = nodeTraverser.getMaxDepth(rootNode)
        val treeStructure = nodeTraverser.printTree(rootNode)

        return SchemaAnalysisResult(
            schemaName = schemaName,
            totalNodes = allPaths.size,
            formFields = formFields,
            compositionNodes = compositionNodes,
            conditionalNodes = conditionalNodes,
            allPaths = allPaths,
            maxDepth = maxDepth,
            treeStructure = treeStructure
        )
    }

    /**
     * Logs comprehensive schema analysis to Android Log with beautiful formatting
     */
    fun logSchemaAnalysis(analysis: SchemaAnalysisResult, tag: String = "SchemaAnalyzer") {
        // Log schema statistics
        Log.d(tag, "📊 Schema Analysis for '${analysis.schemaName}':")
        Log.d(tag, "   Total nodes: ${analysis.totalNodes}")
        Log.d(tag, "   Form fields: ${analysis.formFields.size}")
        Log.d(tag, "   Choice points: ${analysis.compositionNodes.size}")
        Log.d(tag, "   Conditional logic: ${analysis.conditionalNodes.size}")
        Log.d(tag, "   Maximum depth: ${analysis.maxDepth}")

        // Log all form fields for UI generation
        Log.d(tag, "📝 Form Fields:")
        analysis.formFields.forEach { field ->
            val fieldInfo = getFieldInfo(field)
            Log.d(tag, "   $fieldInfo")
        }

        // Log composition nodes for choice handling
        if (analysis.compositionNodes.isNotEmpty()) {
            Log.d(tag, "🔀 Composition Nodes:")
            analysis.compositionNodes.forEach { composition ->
                val compositionNode = composition.node as DescriptorNode.CompositionNode
                Log.d(
                    tag,
                    "   ${composition.key}: ${compositionNode.compositionType} with ${compositionNode.schemas.size} options"
                )
            }
        }

        // Log conditional nodes for dynamic behavior
        if (analysis.conditionalNodes.isNotEmpty()) {
            Log.d(tag, "🔀 Conditional Logic:")
            analysis.conditionalNodes.forEach { conditional ->
                Log.d(tag, "   Conditional rule at: ${conditional.path}")
            }
        }

        // Print beautiful tree structure
        Log.d(tag, "🌳 Schema Tree:")
        analysis.treeStructure.lines().forEach { line ->
            Log.d(tag, line)
        }

        // Extract and log schema paths for navigation
        Log.d(tag, "📍 All Paths:")
        analysis.allPaths.forEach { nodePath ->
            Log.d(tag, "   ${nodePath.path} -> ${nodePath.nodeType}")
        }
    }

    /**
     * Performs complete analysis and logs everything with beautiful formatting.
     * This is the main method to call for comprehensive schema analysis.
     */
    fun performCompleteAnalysis(
        rootNode: DescriptorNode,
        schemaName: String,
        tag: String = "SchemaAnalyzer"
    ) {
        // Get comprehensive schema analysis
        val analysis = analyzeSchema(rootNode, schemaName)

        // Log the basic analysis
        logSchemaAnalysis(analysis, tag)

        // Log the parsed schema structure
        Log.d(tag, "📋 Schema Structure:")
        Log.d(tag, rootNode.toString())

        // Demonstrate advanced analysis capabilities
        logAdvancedAnalysis(analysis, tag)
    }

    /**
     * Gets form fields grouped by their parent sections for UI organization
     */
    fun getFormFieldsBySection(analysis: SchemaAnalysisResult): Map<String, List<NodeTraverser.NodePath>> {
        val sections = mutableMapOf<String, MutableList<NodeTraverser.NodePath>>()

        analysis.formFields.forEach { field ->
            val parentSection = field.path.split(".").getOrNull(1) ?: "root"
            sections.getOrPut(parentSection) { mutableListOf() }.add(field)
        }

        return sections
    }

    /**
     * Extracts validation rules from conditional nodes
     */
    fun extractValidationRules(analysis: SchemaAnalysisResult): List<ValidationRule> {
        return analysis.conditionalNodes.map { conditionalPath ->
            val conditionalNode = conditionalPath.node as DescriptorNode.ConditionalNode

            ValidationRule(
                path = conditionalPath.path,
                condition = conditionalNode.ifSchema?.let { getNodeDescription(it) },
                thenAction = conditionalNode.thenSchema?.let { getNodeDescription(it) },
                elseAction = conditionalNode.elseSchema?.let { getNodeDescription(it) }
            )
        }
    }

    /**
     * Gets schema complexity metrics
     */
    fun getComplexityMetrics(analysis: SchemaAnalysisResult): ComplexityMetrics {
        return ComplexityMetrics(
            totalNodes = analysis.totalNodes,
            leafNodeRatio = analysis.formFields.size.toFloat() / analysis.totalNodes,
            averageDepth = analysis.allPaths.map { it.depth }.average().toFloat(),
            maxDepth = analysis.maxDepth,
            compositionComplexity = analysis.compositionNodes.sumOf {
                (it.node as DescriptorNode.CompositionNode).schemas.size
            },
            conditionalComplexity = analysis.conditionalNodes.size
        )
    }

    /**
     * Logs advanced analysis including complexity metrics, sections, and validation rules
     */
    private fun logAdvancedAnalysis(analysis: SchemaAnalysisResult, tag: String) {
        // Get complexity metrics
        val complexityMetrics = getComplexityMetrics(analysis)
        Log.d(tag, "📊 Complexity Metrics:")
        Log.d(tag, "   Complexity Score: ${complexityMetrics.complexityScore}")
        Log.d(tag, "   Leaf Node Ratio: ${complexityMetrics.leafNodeRatio}")
        Log.d(tag, "   Average Depth: ${complexityMetrics.averageDepth}")
        Log.d(tag, "   Composition Complexity: ${complexityMetrics.compositionComplexity}")
        Log.d(tag, "   Conditional Complexity: ${complexityMetrics.conditionalComplexity}")

        // Get form fields by section for UI organization
        val fieldsBySection = getFormFieldsBySection(analysis)
        if (fieldsBySection.isNotEmpty()) {
            Log.d(tag, "📋 Fields by Section:")
            fieldsBySection.forEach { (section, fields) ->
                Log.d(tag, "   Section '$section': ${fields.size} fields")
                fields.forEach { field ->
                    Log.d(tag, "     - ${field.key} (${field.nodeType})")
                }
            }
        }

        // Extract validation rules
        val validationRules = extractValidationRules(analysis)
        if (validationRules.isNotEmpty()) {
            Log.d(tag, "✅ Validation Rules:")
            validationRules.forEach { rule ->
                Log.d(tag, "   Rule at ${rule.path}:")
                Log.d(tag, "     Condition: ${rule.condition}")
                Log.d(tag, "     Then: ${rule.thenAction}")
                rule.elseAction?.let {
                    Log.d(tag, "     Else: ${it}")
                }
            }
        }

        // Log summary for quick overview
        Log.d(tag, "📋 Analysis Summary:")
        Log.d(tag, "   ✅ Schema '${analysis.schemaName}' analyzed successfully")
        Log.d(tag, "   📊 Complexity Score: ${complexityMetrics.complexityScore}")
        Log.d(tag, "   📝 ${analysis.formFields.size} form fields found")
        Log.d(tag, "   🔀 ${analysis.compositionNodes.size} choice points")
        Log.d(tag, "   🔀 ${analysis.conditionalNodes.size} conditional rules")
        Log.d(tag, "   📏 Maximum depth: ${analysis.maxDepth}")
    }

    private fun getFieldInfo(field: NodeTraverser.NodePath): String {
        return when (val node = field.node) {
            is DescriptorNode.StringNode -> {
                "📝 ${field.key}: String field '${node.title ?: field.key}' at ${field.path}"
            }

            is DescriptorNode.NumberNode -> {
                "🔢 ${field.key}: Number field '${node.title ?: field.key}' at ${field.path}"
            }

            is DescriptorNode.BooleanNode -> {
                "☑️ ${field.key}: Boolean field '${node.title ?: field.key}' at ${field.path}"
            }

            else -> {
                "❓ ${field.key}: ${field.nodeType} at ${field.path}"
            }
        }
    }

    private fun getNodeDescription(node: DescriptorNode): String {
        return when (node) {
            is DescriptorNode.GroupNode -> {
                val fieldCount = node.nodes?.size ?: 0
                "$fieldCount fields"
            }

            is DescriptorNode.StringNode -> "string field '${node.title ?: node.key}'"
            is DescriptorNode.NumberNode -> "number field '${node.title ?: node.key}'"
            is DescriptorNode.BooleanNode -> "boolean field '${node.title ?: node.key}'"
            is DescriptorNode.CompositionNode -> "${node.compositionType} with ${node.schemas.size} options"
            is DescriptorNode.ConditionalNode -> "conditional logic"
        }
    }

    /**
     * Data class representing a validation rule extracted from conditional nodes
     */
    data class ValidationRule(
        val path: String,
        val condition: String?,
        val thenAction: String?,
        val elseAction: String?
    )

    /**
     * Data class representing schema complexity metrics
     */
    data class ComplexityMetrics(
        val totalNodes: Int,
        val leafNodeRatio: Float,
        val averageDepth: Float,
        val maxDepth: Int,
        val compositionComplexity: Int,
        val conditionalComplexity: Int
    ) {
        val complexityScore: Float
            get() = (maxDepth * 0.3f + compositionComplexity * 0.4f + conditionalComplexity * 0.3f)
    }
}
