/*
 * Menu.cpp
 * -----------------------------------------
 * Handles user interaction through a text-based menu for the Course Planner.
 * Allows users to:
 *   1. Load course data from a file
 *   2. Print all available courses in sorted order
 *   3. View details for a specific course
 *   4. Plan courses using topological order
 *   5. Filter courses using advanced search
 *   9. Exit the program
 *
 * Dependencies:
 *   - AVLTree (stores and manages courses)
 *   - CourseLoader (loads data from CSV)
 *   - FilterUtils (filter helper functions)
 *   - DAGPlanner (topological sort)
 */

#include "Menu.h"
#include "CourseLoader.h"
#include "StringUtils.h"
#include "DAGPlanner.h"
#include "FilterUtils.h"
#include <iostream>
#include <limits>
#include <algorithm>

using namespace std;

void displayMenu(AVLTree& tree) {
    int choice = 0;
    string filename;

    bool dataLoaded = false; // flag to prevent actions if course data isn't loaded

    // Display menu options
    while (choice != 9) {
        cout << "\nMenu:\n";
        cout << " 1. Load course data\n";
        cout << " 2. Print all courses\n";
        cout << " 3. Print course info\n";
        cout << " 4. Plan courses (topological order)\n";
        cout << " 5. Filter courses\n";
        cout << " 9. Exit\n";
        cout << "Enter choice: ";

        if (!(cin >> choice)) {
            cin.clear(); // clear error state
            cin.ignore(numeric_limits<streamsize>::max(), '\n'); // discard bad input
            cout << "Invalid input. Please enter a number: 1 - 5, 9.\n";
            continue;
        }

        switch (choice) {
        // Load course data from file
        case 1:
            cout << "Enter filename (type in course_data.csv (testing purpose only)): ";
            cin >> filename;
            loadCourses(filename, tree);
            dataLoaded = true; // mark data as loaded
            break;

        // Print all courses in sorted (in-order) order
        case 2: {
            if (!dataLoaded) {
                cout << "Please load course data first.\n";
                break;
            }

            vector<Course> courses = tree.inOrder();
            for (const auto& course : courses) {
                cout << course.courseId << ", " << course.title << endl;
            }
            break;
        }
        // Print detail info for a specific course
        case 3: {
            if (!dataLoaded) {
                cout << "Please load course data first.\n";
                break;
            }

            string courseId;
            cout << "Enter course ID: ";
            cin >> ws;
            getline(cin, courseId);
            trim(courseId);
            transform(courseId.begin(), courseId.end(), courseId.begin(), ::toupper); // Case-insensitive search

            Course* course = tree.search(courseId);
            if (course) {
                cout << "Course ID: " << course->courseId << endl;
                cout << "Title: " << course->title << endl;
                cout << "Prerequisites: ";
                if (course->prerequisites.empty()) {
                    cout << "None";
                }
                else {
                    for (const auto& prereq : course->prerequisites)
                        cout << prereq << " ";
                }
                cout << endl;
            }
            else {
                cout << "Course not found.\n";
            }
            break;
        }
        // Plan course sequence (topological sort)
        case 4: {
            if (!dataLoaded) {
                cout << "Please load course data first.\n";
                break;
            }

            vector<Course> allCourses = tree.inOrder();
            DAGPlanner planner;
            planner.buildFromCourses(allCourses);

            if (planner.hasCycle()) {
                cout << "Cycle detected in course prerequisites. Cannot plan courses.\n";
            }
            else {
                vector<string> plan = planner.getCoursePlan();
                cout << "Course Plan:\n";
                for (const string& courseId : plan) {
                    Course* c = tree.search(courseId);
                    if (c) cout << courseId << " - " << c->title << endl;
                }
            }
            break;
        }
        // Filter courses by user criteria
        case 5: {
            if (!dataLoaded) {
                cout << "Please load course data first.\n";
                break;
            }

            vector<Course> allCourses = tree.inOrder();
            int filterChoice;

            cout << "\nFilter Options:\n";
            cout << " 1. By prefix (e.g., CS)\n";
            cout << " 2. By number of prerequisites\n";
            cout << " 3. By keyword in title\n";
            cout << "Enter filter type: ";
            if (!(cin >> filterChoice)) {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                cout << "Invalid input.\n";
                break;
            }

            switch (filterChoice) {
            case 1: {
                string prefix;
                cout << "Enter prefix: ";
                cin >> prefix;
                transform(prefix.begin(), prefix.end(), prefix.begin(), ::toupper); // Case-insensitive
                vector<Course> results = FilterUtils::filterByPrefix(allCourses, prefix);
                if (results.empty()) {
                    cout << "No courses found with prefix \"" << prefix << "\".\n";
                }
                else {
                    for (const auto& c : results)
                        cout << c.courseId << ", " << c.title << endl;
                }
                break;
            }
            case 2: {
                size_t count;
                cout << "Enter number of prerequisites: ";
                if (!(cin >> count)) {
                    cin.clear();
                    cin.ignore(numeric_limits<streamsize>::max(), '\n');
                    cout << "Invalid number.\n";
                    break;
                }
                vector<Course> results = FilterUtils::filterByPrerequisiteCount(allCourses, count);
                if (results.empty()) {
                    cout << "No courses found with " << count << " prerequisites.\n";
                }
                else {
                    for (const auto& c : results)
                        cout << c.courseId << ", " << c.title << endl;
                }
                break;
            }
            case 3: {
                string keyword;
                cout << "Enter keyword: ";
                cin >> ws;
                getline(cin, keyword);
                trim(keyword);
                vector<Course> results = FilterUtils::filterByKeyword(allCourses, keyword);
                if (results.empty()) {
                    cout << "No courses found with keyword \"" << keyword << "\" in the title.\n";
                }
                else {
                    for (const auto& c : results)
                        cout << c.courseId << ", " << c.title << endl;
                }
                break;
            }
            default:
                cout << "Invalid filter option.\n";
            }
            break;
        }
        // Exit
        case 9:
            cout << "Exiting...\n";
            break;

        // Catch invalid menu selections
        default:
            cout << "Invalid choice. Please enter a number 1 - 5, 9.\n";
        }
    }
}
