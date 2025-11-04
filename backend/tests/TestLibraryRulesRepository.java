import java.util.*;

/**
 * Test for LibraryRulesRepository
 */
public class TestLibraryRulesRepository {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Testing LibraryRulesRepository");
        System.out.println("=".repeat(60));

        LibraryRulesRepository repo = new LibraryRulesRepository();

        // Test 1: getAllRules()
        testGetAllRules(repo);

        // Test 2: getRulesByCategory()
        testGetRulesByCategory(repo);

        // Test 3: searchRules()
        testSearchRules(repo);

        // Test 4: getAllCategories()
        testGetAllCategories(repo);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("All tests passed! ✅");
        System.out.println("=".repeat(60));
    }

    static void testGetAllRules(LibraryRulesRepository repo) {
        System.out.println("\n[Test 1] getAllRules()");
        List<LibraryRulesRepository.LibraryRule> rules = repo.getAllRules();

        assert rules != null : "Rules should not be null";
        assert rules.size() >= 10 : "Should have at least 10 rules";

        System.out.println("✅ Found " + rules.size() + " rules");

        // Show first 3 rules
        System.out.println("\nSample rules:");
        for (int i = 0; i < Math.min(3, rules.size()); i++) {
            LibraryRulesRepository.LibraryRule rule = rules.get(i);
            System.out.println("  " + (i+1) + ". " + rule);
        }
    }

    static void testGetRulesByCategory(LibraryRulesRepository repo) {
        System.out.println("\n[Test 2] getRulesByCategory()");

        String category = "借閱規則";
        List<LibraryRulesRepository.LibraryRule> rules = repo.getRulesByCategory(category);

        assert rules != null : "Rules should not be null";
        assert rules.size() > 0 : "Should have rules in category: " + category;

        // Check all rules belong to the category
        for (LibraryRulesRepository.LibraryRule rule : rules) {
            assert rule.category.equals(category) :
                "Rule category should be " + category;
        }

        System.out.println("✅ Found " + rules.size() + " rules in category: " + category);
    }

    static void testSearchRules(LibraryRulesRepository repo) {
        System.out.println("\n[Test 3] searchRules()");

        String keyword = "借書";
        List<LibraryRulesRepository.LibraryRule> rules = repo.searchRules(keyword);

        assert rules != null : "Rules should not be null";
        assert rules.size() > 0 : "Should find rules with keyword: " + keyword;

        System.out.println("✅ Found " + rules.size() + " rules with keyword: " + keyword);

        // Show matching rules
        System.out.println("\nMatching rules:");
        for (LibraryRulesRepository.LibraryRule rule : rules) {
            System.out.println("  - " + rule.question);
        }
    }

    static void testGetAllCategories(LibraryRulesRepository repo) {
        System.out.println("\n[Test 4] getAllCategories()");

        List<String> categories = repo.getAllCategories();

        assert categories != null : "Categories should not be null";
        assert categories.size() > 0 : "Should have at least one category";

        System.out.println("✅ Found " + categories.size() + " categories");

        System.out.println("\nAll categories:");
        for (String category : categories) {
            System.out.println("  - " + category);
        }
    }
}
