// DAGPlanner.cpp
// -----------------------------------------
// Builds and processes a Directed Acyclic Graph (DAG) of course prerequisites.
// Enables topological sorting to plan courses and detects cycles in course dependencies.

#include "DAGPlanner.h"
#include <iostream>

// Build the adjacency list graph from a list of courses.
// Each course points to other courses that depend on it.
void DAGPlanner::buildFromCourses(const std::vector<Course>& courses) {
    adjList.clear();

    for (const auto& course : courses) {
        // Create directed edges from each prerequisite to the current course
        for (const auto& prereq : course.prerequisites) {
            adjList[prereq].push_back(course.courseId); // prereq -> course
        }
        // Ensure each course appears as a key, even if it has no outgoing edges
        if (adjList.find(course.courseId) == adjList.end()) {
            adjList[course.courseId] = {};
        }
    }
}

// Recursive helper for topological sort using DFS.
// Marks nodes visited and pushes them to the stack once all dependencies are resolved.
void DAGPlanner::topologicalSortUtil(
    const std::string& courseId,
    std::unordered_set<std::string>& visited,
    std::stack<std::string>& Stack
) {
    visited.insert(courseId);

    // Visit all the neighboring courses (i.e., courses that depend on this one)
    for (const auto& neighbor : adjList[courseId]) {
        if (visited.find(neighbor) == visited.end()) {
            topologicalSortUtil(neighbor, visited, Stack);
        }
    }
    // Push to stack after visiting all neighbors (postorder)
    Stack.push(courseId);
}

// Public method to get the list of courses in topological order (course plan).
std::vector<std::string> DAGPlanner::getCoursePlan() {
    std::unordered_set<std::string> visited;
    std::stack<std::string> Stack;

    // Visit all nodes in the graph to cover disconnected components
    for (const auto& pair : adjList) {
        if (visited.find(pair.first) == visited.end()) {
            topologicalSortUtil(pair.first, visited, Stack);
        }
    }
    // Convert stack to vector for final result
    std::vector<std::string> order;
    while (!Stack.empty()) {
        order.push_back(Stack.top());
        Stack.pop();
    }
    return order;
}

// Recursive utility for cycle detection using DFS.
// Tracks both visited nodes and the current recursion stack to detect back edges.
bool DAGPlanner::isCyclicUtil(
    const std::string& node,
    std::unordered_set<std::string>& visited,
    std::unordered_set<std::string>& recStack
) {
    visited.insert(node);
    recStack.insert(node); // Track current recursion path

    for (const auto& neighbor : adjList[node]) {
        // Cycle found via back edge
        if (recStack.find(neighbor) != recStack.end())
            return true;

        // Recursively visit unvisited neighbors
        if (visited.find(neighbor) == visited.end() &&
            isCyclicUtil(neighbor, visited, recStack)) {
            return true;
        }
    }
    // Backtrack: remove node from current recursion stack
    recStack.erase(node);
    return false;
}

// Detect if the graph has any cycles.
// Returns true if a cycle is detected; false otherwise.
bool DAGPlanner::hasCycle() {
    std::unordered_set<std::string> visited;
    std::unordered_set<std::string> recStack;

    // Check for cycles starting from each node
    for (const auto& pair : adjList) {
        if (visited.find(pair.first) == visited.end()) {
            if (isCyclicUtil(pair.first, visited, recStack))
                return true;
        }
    }
    return false; // No cycles found
}
