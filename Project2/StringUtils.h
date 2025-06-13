// StringUtils.h
#ifndef STRING_UTILS_H
#define STRING_UTILS_H

#include <string>
#include <algorithm>
#include <cctype>

// Removes leading whitespace
inline void ltrim(std::string& s) {
    s.erase(s.begin(), std::find_if(s.begin(), s.end(),
        [](unsigned char ch) { return !std::isspace(ch); }));
}

// Removes trailing whitespace
inline void rtrim(std::string& s) {
    s.erase(std::find_if(s.rbegin(), s.rend(),
        [](unsigned char ch) { return !std::isspace(ch); }).base(), s.end());
}

// Removes leading and trailing whitespace
inline void trim(std::string& s) {
    ltrim(s);
    rtrim(s);
}

#endif





