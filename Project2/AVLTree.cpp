/*
 * AVLTree.cpp
 * ----------------------------------
 * Implementation of a self-balancing binary search tree (AVL Tree)
 * to store and manage Course objects using courseId as the key.
 *
 * Features:
 * - Automatically balances after insertions using rotations
 * - Efficient search and sorted in-order traversal
 * - Proper memory cleanup via recursive destruction
 *
 * Key Functions:
 * - insert(): Adds a Course while maintaining AVL balance
 * - search(): Finds a Course by ID
 * - inOrder(): Returns sorted list of Courses
 *
 */

#include "AVLTree.h"
#include <algorithm>

// Node constructor initializes a Leaf with height = 1
AVLTree::Node::Node(const Course& c)
    : course(c), left(nullptr), right(nullptr), height(1) {}

// Initiakize emty tree
AVLTree::AVLTree() : root(nullptr) {}

// Clean up all dynamically allocated nodes
AVLTree::~AVLTree() {
    destroy(root);
}

int AVLTree::getHeight(Node* node) {
    return node ? node->height : 0;
}
 
int AVLTree::getBalance(Node* node) {
    return node ? getHeight(node->left) - getHeight(node->right) : 0;
}

AVLTree::Node* AVLTree::rightRotate(Node* y) {
    Node* x = y->left;
    Node* T2 = x->right;

    // Rotation logic
    x->right = y;
    y->left = T2;

    // Update height
    y->height = 1 + std::max(getHeight(y->left), getHeight(y->right));
    x->height = 1 + std::max(getHeight(x->left), getHeight(x->right));

    return x; // New root
}

AVLTree::Node* AVLTree::leftRotate(Node* x) {
    Node* y = x->right;
    Node* T2 = y->left;

    // Rotation logic
    y->left = x;
    x->right = T2;

    // Update height
    x->height = 1 + std::max(getHeight(x->left), getHeight(x->right));
    y->height = 1 + std::max(getHeight(y->left), getHeight(y->right));

    return y; // New root
}

AVLTree::Node* AVLTree::insert(Node* node, const Course& course) {
    if (!node) return new Node(course);

    if (course.courseId < node->course.courseId)
        node->left = insert(node->left, course);
    else if (course.courseId > node->course.courseId)
        node->right = insert(node->right, course);
    else
        return node; // Duplicate

    // Update height after insert
    node->height = 1 + std::max(getHeight(node->left), getHeight(node->right));

    // Check balance and apply rotation if needed 
    int balance = getBalance(node);

    // LL Case
    if (balance > 1 && course.courseId < node->left->course.courseId)
        return rightRotate(node);
    // RR Case
    if (balance < -1 && course.courseId > node->right->course.courseId)
        return leftRotate(node);
    // LR Case
    if (balance > 1 && course.courseId > node->left->course.courseId) {
        node->left = leftRotate(node->left);
        return rightRotate(node);
    }
    // RL Case
    if (balance < -1 && course.courseId < node->right->course.courseId) {
        node->right = rightRotate(node->right);
        return leftRotate(node);
    }

    return node; // Balanced
}

void AVLTree::insert(const Course& course) {
    root = insert(root, course);
}

void AVLTree::inOrder(Node* node, std::vector<Course>& courses) const {
    if (node) {
        inOrder(node->left, courses);
        courses.push_back(node->course);
        inOrder(node->right, courses);
    }
}

// Get sorted course list
std::vector<Course> AVLTree::inOrder() const {
    std::vector<Course> courses;
    inOrder(root, courses);
    return courses;
}

Course* AVLTree::search(const std::string& courseId) const {
    return search(root, courseId);
}

// Recursive search by courseId
Course* AVLTree::search(Node* node, const std::string& courseId) const {
    if (!node) return nullptr;

    if (courseId == node->course.courseId) return &node->course;
    if (courseId < node->course.courseId)
        return search(node->left, courseId);
    return search(node->right, courseId);
}

// Recursive destruction of nodes
void AVLTree::destroy(Node* node) {
    if (!node) return;
    destroy(node->left);
    destroy(node->right);
    delete node;
}
