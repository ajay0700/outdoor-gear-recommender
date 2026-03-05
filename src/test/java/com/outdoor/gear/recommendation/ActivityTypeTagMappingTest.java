package com.outdoor.gear.recommendation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 活动类型→标签映射单元测试
 */
class ActivityTypeTagMappingTest {

    @Test
    void getTagNamesForActivityType_徒步_returnsExtendedTags() {
        List<String> tags = ActivityTypeTagMapping.getTagNamesForActivityType("徒步");
        assertNotNull(tags);
        assertTrue(tags.contains("徒步"));
        assertTrue(tags.contains("轻量化"));
        assertTrue(tags.contains("便携"));
    }

    @Test
    void getTagNamesForActivityType_露营_returnsExtendedTags() {
        List<String> tags = ActivityTypeTagMapping.getTagNamesForActivityType("露营");
        assertNotNull(tags);
        assertTrue(tags.contains("露营"));
        assertTrue(tags.contains("防水"));
        assertTrue(tags.contains("防风"));
    }

    @Test
    void getTagNamesForActivityType_登山_returnsExtendedTags() {
        List<String> tags = ActivityTypeTagMapping.getTagNamesForActivityType("登山");
        assertNotNull(tags);
        assertTrue(tags.contains("登山"));
        assertTrue(tags.contains("专业级"));
    }

    @Test
    void getTagNamesForActivityType_nullOrBlank_returnsEmpty() {
        assertTrue(ActivityTypeTagMapping.getTagNamesForActivityType(null).isEmpty());
        assertTrue(ActivityTypeTagMapping.getTagNamesForActivityType("").isEmpty());
        assertTrue(ActivityTypeTagMapping.getTagNamesForActivityType("   ").isEmpty());
    }

    @Test
    void getTagNamesForActivityType_unknownType_returnsTypeAsSingleTag() {
        List<String> tags = ActivityTypeTagMapping.getTagNamesForActivityType("未知活动");
        assertEquals(List.of("未知活动"), tags);
    }
}
