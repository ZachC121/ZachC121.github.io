// FilterUtils.cpp
// -----------------------------------------
// Implements filtering utility functions for courses.
// Allows filtering based on course ID prefix, number of prerequisites, and keyword in title.

#include "FilterUtils.h"
#include <algorithm>  

namespace FilterUtils {

    // Filter courses whose courseId starts with the given prefix.
    // Example: prefix = "CS" -> returns courses like "CS101", "CS201"
    std::vector<Course> filterByPrefix(const std::vector<Course>& courses, const std::string& prefix) {
        std::vector<Course> result;
        for (const auto& c : courses) {
            // Check if the courseId starts with the prefix (case-sensitive)
            if (c.courseId.find(prefix) == 0) {
                result.push_back(c);
            }
        }
        return result;
    }

    // Filter courses that have exactly 'count' prerequisites.
    std::vector<Course> filterByPrerequisiteCount(const std::vector<Course>& courses, size_t count) {
        std::vector<Course> result;
        for (const auto& c : courses) {
            if (c.prerequisites.size() == count) {
                result.push_back(c);
            }
        }
        return result;
    }

    // Filter courses where the title contains the given keyword (case-insensitive).
    std::vector<Course> filterByKeyword(const std::vector<Course>& courses, const std::string& keyword) {
        std::vector<Course> result;
        for (const auto& c : courses) {
            // Create lowercase versions of the title and keyword for comparison
            std::string titleLower = c.title;
            std::string keywordLower = keyword;
            std::transform(titleLower.begin(), titleLower.end(), titleLower.begin(), ::tolower);
            std::transform(keywordLower.begin(), keywordLower.end(), keywordLower.begin(), ::tolower);

            // Check if the keyword appears anywhere in the title
            if (titleLower.find(keywordLower) != std::string::npos) {
                result.push_back(c);
            }
        }
        return result;
    }
} 
