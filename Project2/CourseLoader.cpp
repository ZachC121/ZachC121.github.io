/*
 * CourseLoader.cpp
 * -----------------------------------------
 * Loads course data from a CSV file and inserts it into an AVL tree.
 * Each line in the file represents a course in the format:
 *
 *     CourseId,Title,Prereq1,Prereq2,...
 *
 * Features:
 * - Trims whitespace from fields
 * - Handles malformed or incomplete lines gracefully
 * - Adds parsed Course objects to an AVLTree
 *
 */

#include "CourseLoader.h"
#include "StringUtils.h"
#include <fstream>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <cctype>
#include <locale>

// ===== Main File Loader =====
void loadCourses(const std::string& filename, AVLTree& tree) {
    std::ifstream file(filename);
    if (!file) {
        std::cerr << "Could not open file: " << filename << std::endl;
        return;
    }
    std::cout << "File open successfully: " << filename << std::endl;

    if (file.peek() == std::ifstream::traits_type::eof()) {
        std::cerr << "File is empty: " << filename << std::endl;
        return;
    }

    std::string line;
    int lineNumber = 0;
    int malformedCount = 0;

    // Read file line by line 
    while (getline(file, line)) {
        ++lineNumber;
        if (line.empty()) continue;

        std::stringstream ss(line);
        std::string courseId, title, prereq;

        // Read courseId
        if (!getline(ss, courseId, ',')) {
            std::cerr << "Failed to read courseId on line " << lineNumber << std::endl;
            ++malformedCount;
            continue;
        }
        // Read Title
        if (!getline(ss, title, ',')) {
            std::cerr << "Failed to read title on line " << lineNumber << std::endl;
            malformedCount;
            continue;
        }

        if (malformedCount > 0) {
            std::cout << malformedCount << " malformed line(s) skipped.\n";
        }

        // Trim whitespace from courseId and title
        trim(courseId);
        trim(title);

        Course course;
        course.courseId = courseId;
        course.title = title;

        // Read and trim prerequisites
        while (getline(ss, prereq, ',')) {
            trim(prereq);
            if (!prereq.empty())
                course.prerequisites.push_back(prereq);
        }
        if (!tree.search(course.courseId)) {
            tree.insert(course);
        }
        else {
            std::cerr << "Duplicate course ID skipped: " << course.courseId << std::endl;
        }
    }
    file.close();
}