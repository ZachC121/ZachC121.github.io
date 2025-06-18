// FilterUtils.h
#pragma once

#include "Course.h"
#include <vector>
#include <string>

namespace FilterUtils {
    std::vector<Course> filterByPrefix(const std::vector<Course>& courses, const std::string& prefix);
    std::vector<Course> filterByPrerequisiteCount(const std::vector<Course>& courses, size_t count);
    std::vector<Course> filterByKeyword(const std::vector<Course>& courses, const std::string& keyword);
}


