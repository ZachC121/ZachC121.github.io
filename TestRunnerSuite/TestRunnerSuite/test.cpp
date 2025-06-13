/*
 * collection_tests.cpp
 * -------------------------------
 * Google Test Suite for Dynamic Integer Vector Collection
 *
 * This test suite verifies the behavior of a dynamic integer vector (std::vector<int>)
 * under a variety of operations. It includes both functional and edge-case tests, using
 * Google Test's fixture and parameterized testing features.
 *
 * Key Components:
 * - Environment: Global setup to seed random number generator
 * - CollectionTest: Base test fixture providing setup/teardown and helper methods
 * - ParameterizedCollectionTest: Runs tests with multiple collection sizes
 *
 * Test Categories:
 * - Basic Behavior Tests: Verify initialization and pointer validity
 * - Adding Entries: Ensure correct size and value range after adding elements
 * - Resize Tests: Validate vector resizing behavior including edge cases
 * - Clear/Erase/Reserve: Test container manipulation methods
 * - Access Tests: Ensure proper exception throwing on invalid access
 * - Parameterized Tests: Test behavior across various collection sizes
 * - Edge Case Tests: Validate response to invalid input and capacity quirks
 *
 * Note:
 * - Includes intentionally failing tests to demonstrate test failure handling
 */

#include "pch.h"
#include "gtest/gtest.h"
#include <limits>

const int MAX_ENTRY_VALUE = 100;
const int TEST_ENTRY_COUNT = 5;
const int INVALID_INDEX = 999;

// Global test environment setup
class Environment : public ::testing::Environment {
public:
    void SetUp() override {
        srand(static_cast<unsigned int>(time(nullptr)));
    }
    void TearDown() override {}
};

// Base test class for vector collection
class CollectionTest : public ::testing::Test {
protected:
    std::unique_ptr<std::vector<int>> collection;

    void SetUp() override {
        collection = std::make_unique<std::vector<int>>();
    }

    void TearDown() override {
        collection->clear();
        collection.reset();
    }

    // Helper to add valid random entries
    void add_entries(int count) {
        if (count < 0) throw std::invalid_argument("count must be non-negative");
        for (int i = 0; i < count; ++i)
            collection->push_back(rand() % MAX_ENTRY_VALUE);
    }

    // Helper to verify all values are within range
    void expect_values_in_range() {
        for (const int& val : *collection) {
            EXPECT_GE(val, 0);
            EXPECT_LT(val, MAX_ENTRY_VALUE);
        }
    }
};

// Parameterized test class for variable-size vectors
struct ParameterizedCollectionTest : public CollectionTest, public ::testing::WithParamInterface<int> {
    ParameterizedCollectionTest() = default;
};

INSTANTIATE_TEST_CASE_P(Sizes, ParameterizedCollectionTest, ::testing::Values(0, 1, 5, 10));

// ========== Basic Behavior Tests ==========
TEST_F(CollectionTest, PointerNotNullOnCreate) {
    ASSERT_TRUE(collection);
    ASSERT_NE(collection.get(), nullptr);
}

TEST_F(CollectionTest, EmptyOnCreate) {
    ASSERT_TRUE(collection->empty());
    ASSERT_EQ(collection->size(), 0);
}

// Failing test to verify test framework
TEST_F(CollectionTest, AlwaysFails) {
    FAIL();
}

// ========== Adding Entries ==========
TEST_F(CollectionTest, AddSingleEntry) {
    add_entries(1);
    EXPECT_EQ(collection->size(), 1);
    expect_values_in_range();
}

TEST_F(CollectionTest, AddFiveEntries) {
    add_entries(TEST_ENTRY_COUNT);
    EXPECT_EQ(collection->size(), TEST_ENTRY_COUNT);
    expect_values_in_range();
}

TEST_F(CollectionTest, AddMultipleEntries) {
    const int entryCount = 7;
    add_entries(entryCount);
    EXPECT_EQ(collection->size(), entryCount);
    expect_values_in_range();
}

// ========== Resize Tests ==========
TEST_F(CollectionTest, ResizeIncrease) {
    collection->resize(10);
    EXPECT_GE(collection->size(), 10);
}

// INTENTIONAL ERROR: Expect wrong size after resize decrease
TEST_F(CollectionTest, ResizeToDecrease) {
    add_entries(10);
    collection->resize(5);
    EXPECT_EQ(collection->size(), 10); // Should fail, expected 5
}

TEST_F(CollectionTest, ResizeDecrease) {
    add_entries(10);
    collection->resize(5);
    EXPECT_EQ(collection->size(), 5);
}

TEST_F(CollectionTest, ResizeToZero) {
    add_entries(5);
    collection->resize(0);
    EXPECT_EQ(collection->size(), 0);
}

// ========== Clear/Erase/Reserve ==========
TEST_F(CollectionTest, ClearEmptiesCollection) {
    add_entries(5);
    collection->clear();
    EXPECT_TRUE(collection->empty());
}

TEST_F(CollectionTest, EraseAllElements) {
    add_entries(5);
    collection->erase(collection->begin(), collection->end());
    EXPECT_TRUE(collection->empty());
}

TEST_F(CollectionTest, ReserveIncreasesCapacityOnly) {
    size_t old_capacity = collection->capacity();
    collection->reserve(old_capacity + 10);
    EXPECT_GT(collection->capacity(), old_capacity);
    EXPECT_EQ(collection->size(), 0);
}

// ========== Access Tests ==========
TEST_F(CollectionTest, AtThrowsOutOfRange) {
    ASSERT_THROW(collection->at(INVALID_INDEX), std::out_of_range);
}

TEST_F(CollectionTest, AtThrowsWhenIndexTooHigh) {
    add_entries(3);
    ASSERT_THROW(collection->at(INVALID_INDEX), std::out_of_range);
}

// ========== Parameterized Tests ==========
TEST_P(ParameterizedCollectionTest, MaxSizeGTECurrentSize) {
    add_entries(GetParam());
    EXPECT_EQ(collection->size(), GetParam());
    EXPECT_GT(collection->max_size(), collection->size());
}

TEST_P(ParameterizedCollectionTest, CapacityGTECurrentSize) {
    add_entries(GetParam());
    EXPECT_EQ(collection->size(), GetParam());
    EXPECT_GE(collection->capacity(), collection->size());
}

// ========== Edge Case Tests ==========
TEST_F(CollectionTest, AddNegativeEntriesThrows) {
    ASSERT_THROW(add_entries(-1), std::invalid_argument);
}

TEST_F(CollectionTest, ReserveNegativeSizeHasNoEffect) {
    size_t original_capacity = collection->capacity();
    collection->reserve(static_cast<size_t>(-1));
    EXPECT_GE(collection->capacity(), original_capacity);  // should be safe due to unsigned wrap-around
}

