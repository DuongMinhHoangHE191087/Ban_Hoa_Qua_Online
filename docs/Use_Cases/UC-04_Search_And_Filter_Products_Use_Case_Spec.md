# Use Case Specification
## Online Fruit Shop System
### UC-04 Search and Filter Products

## 1. Management Information
**ID and Name:** UC-04 Search and Filter Products
**Created By:** Duong Minh Hoang
**Date Created:** May 19, 2026
**Feature:** Product Discovery -> Search and Filter

## 2. Actor Definitions & Purpose
**Primary Actor:** Guest, Customer
**Secondary Actors:** None
**Description:** Allows users to find specific fruits or categories by entering keywords into a search bar, or by applying multiple criteria filters (e.g., price range, fruit category, region/origin, customer ratings). This enables users to quickly locate desired items within the extensive catalog of the Online Fruit Shop System.

## 3. Execution Conditions
**Trigger:** The user enters a keyword into the Search Bar and presses Enter/Search, OR the user interacts with the filtering sidebar on the Product Listing Page.
**Preconditions:**
- PRE-1: The platform contains published, active products.
- PRE-2: The search/filter engine (or database) is functioning normally.
**Postconditions:**
- POST-1: The system compiles and displays a categorized, paginated list of products matching the user's criteria.
- POST-2: If no products match, the system provides a graceful fallback state with related recommendations.

## 4. Scenarios (Flow of Events)
### Normal Flow (Keyword Search):
1. The user navigates to the Home Page or any page showing the top navigation search bar.
2. The user types a keyword (e.g., "Apple", "Organic", "Dalat") into the search input.
3. The user clicks the Search icon or presses Enter.
4. The system processes the keyword against active product titles, descriptions, categories, and tags.
5. The system filters out products that are disabled or hidden by Shop Owners.
6. The system sorts the resulting dataset by relevance (default) or the user's pre-selected sorting criteria.
7. The system redirects the user to the Product Listing Page (Search Results Page).
8. The system displays the matching products, indicating essential information: Image, Title, Price, Average Rating, and Stock Status.

### Alternative Flows:
**4.1. Applying Multi-Criteria Filters**
1. The user is on the Product Listing Page.
2. The system displays a sidebar or modal featuring various filters (Price Range, Fruit Category, Origin/Region, Ratings).
3. The user selects one or multiple checkboxes/slider values (e.g., Price: 100k-500k, Origin: Imported).
4. The user clicks "Apply Filters" (or the system dynamically updates results via AJAX).
5. The system executes a composed query based on the selected criteria and applies rule INV-03 (handling Out of Stock items).
6. The system refreshes the product grid to show only items fulfilling all conditions.

**4.2. Sorting Results**
1. On the Product Listing Page, the user clicks the "Sort By" drop-down menu.
2. The user selects an option (e.g., Price: Low to High, Newest, Top Rated).
3. The system reorganizes the current search/filtered list immediately.

### Exceptions / Error Handling:
**4.E1 No Products Found**
1. At step 4 of the Normal Flow or step 5 of Flow 4.1, the query returns an empty result set (0 matching products).
2. The system displays a friendly message: "We couldn't find any products matching your search/filters."
3. The system suggests clearing filters, checking spelling, or browsing featured/recommended products.

**4.E2 Database/Search Engine Timeout**
1. At step 4 of the Normal Flow, the system fails to retrieve results within the accepted timeframe due to high load or service failure.
2. The system aborts the operation and displays a generic warning: "Search is currently unavailable. Please try again in a few moments."
3. The system logs the error to the monitoring service.

## 5. Additional Information
**Priority:** High (P0 - Core functionality)
**Frequency of Use:** Very High. A primary interaction pattern used by almost every visitor.
**Business Rules:**
- INV-03: If all variants of a product have Stock = 0, the system may hide it from general recommendations, though it might still appear in direct keyword searches marked explicitly as "Out of Stock" (depending on UX/Marketing configuration).
- PRD-03: The system may automatically boost or tag products designated under active "Seasonal" campaigns in the search results depending on the current date.
**Other Information:** The search function should Ideally support partial matches, typo tolerance (fuzzy matching), and autocomplete suggestions (drop-down list) as the user types. 
**Assumptions:** Search results respect data visibility; Draft products, suspended shops, and globally disabled categories will never appear in standard user search results.