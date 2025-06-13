#ifndef AVL_TREE_H
#define AVL_TREE_H

#include "Course.h"
#include <vector>

class AVLTree {
private:
    struct Node {
        Course course;
        Node* left;
        Node* right;
        int height;
        Node(const Course& c);
    };

    Node* root;

    int getHeight(Node* node);
    int getBalance(Node* node);
    Node* rightRotate(Node* y);
    Node* leftRotate(Node* x);
    Node* insert(Node* node, const Course& course);
    void inOrder(Node* node, std::vector<Course>& courses) const;
    Course* search(Node* node, const std::string& courseId) const;
    void destroy(Node* node);

public:
    AVLTree();
    ~AVLTree();

    void insert(const Course& course);
    std::vector<Course> inOrder() const;
    Course* search(const std::string& courseId) const;
};

#endif // !AVL_TREE_H

