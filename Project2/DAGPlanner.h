// DAGPlanner class
// -----------------------------------------
// Responsible for building a Directed Acyclic Graph (DAG) of course prerequisites,
// detecting cycles, and generating a valid course plan (topological order).

#pragma once

#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <stack>
#include "Course.h"

class DAGPlanner {
public:
    // Builds the course dependency graph from a list of courses.
    // Each prerequisite becomes a directed edge: prereq -> course.
    void buildFromCourses(const std::vector<Course>& courses);

    // Returns a valid topological ordering of courses
    // assuming there are no cycles in the graph.
    std::vector<std::string> getCoursePlan();

    // Returns true if a cycle exists in the graph (invalid course plan).
    bool hasCycle();

private:
    // Adjacency list representation of the DAG:
    // key = courseId, value = list of courses that depend on this course.
    std::unordered_map<std::string, std::vector<std::string>> adjList;

    // Recursive DFS utility for topological sorting.
    void topologicalSortUtil(
        const std::string& courseId,
        std::unordered_set<std::string>& visited,
        std::stack<std::string>& Stack);

    // Recursive DFS utility for detecting cycles.
    bool isCyclicUtil(
        const std::string& node,
        std::unordered_set<std::string>& visited,
        std::unordered_set<std::string>& recStack);
};
