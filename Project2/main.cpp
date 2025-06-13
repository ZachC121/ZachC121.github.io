/*
 * main.cpp
 * -----------------------------------------
 * Entry point for the Course Planner application.
 * Initializes the AVL Tree and launches the user menu.
 *
 */

#include "Menu.h"

int main() {
	AVLTree courseTree;
	displayMenu(courseTree);
	return 0;
}