#ifndef COURSE_H
#define COURSE_H

#include <string>
#include <vector>

struct Course {
    std::string courseId;
    std::string title;
    std::vector<std::string> prerequisites;
};

#endif // !COURSE_H