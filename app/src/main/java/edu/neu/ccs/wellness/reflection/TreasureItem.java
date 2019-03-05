package edu.neu.ccs.wellness.reflection;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hermansaksono on 3/1/19.
 */
@IgnoreExtraProperties
public class TreasureItem {

    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_TITLE = "title";
    public static final String KEY_LAST_UPDATE_TIMESTAMP = "lastUpdateTimestamp";
    public static final String KEY_PARENT_ID = "parentId";
    public static final String KEY_SUBPARENT_ID = "subparentId";
    public static final String KEY_TYPE = "type";
    public static final String KEY_CONTENTS = "contents";
    public static final String KEY_INCARNATION = "iterationId";
    public static final String DEFAULT_SUBPARENT_NAME = "Unset";
    public static final String DEFAULT_STRING_ID = "default%s_g%s";
    public static final String STORY_REFLECTION_STRING_ID = "story%s_g%s";
    public static final String RESOLUTION_STRING_ID = "resolution%s_g%s";
    public static final String ITERATION_STRING_ID = "iter%d_%s";


    @PropertyName(value=KEY_TITLE)
    private String title;

    @PropertyName(value=KEY_TYPE)
    private int type = TreasureItemType.STORY_REFLECTION;

    @PropertyName(value=KEY_PARENT_ID)
    private String parentId;

    @PropertyName(value=KEY_SUBPARENT_ID)
    private String subparentId;

    @PropertyName(value = KEY_INCARNATION)
    private int iterationId = 0;

    @PropertyName(value = KEY_CONTENTS)
    private Map<String, String> contents = new HashMap<>();

    @PropertyName(value = KEY_LAST_UPDATE_TIMESTAMP)
    private long lastUpdateTimestamp;

    /* CONSTRUCTORS */
    public TreasureItem () {

    }

    /* BASE METHODS */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public String getParentId() {
        return parentId;
    }

    public String getSubparentId() {
        return subparentId;
    }

    public int getIterationId() {
        return iterationId;
    }

    public Map<String, String> getContents() {
        return contents;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Exclude
    public String getStringId() {
        return getStringId(this.parentId, this.subparentId, this.iterationId, this.type);
    }

    /* STATIC HELPER METHODS */
    /**
     * Returns the {@link String} id of a {@link TreasureItem} given its parentId and subParentId.
     * @param parentId
     * @param subParentId
     * @param type
     * @return
     */
    public static String getStringId(String parentId, String subParentId, int iteration, int type) {
        String stringId;
        switch (type) {
            case TreasureItemType.DEFAULT:
                stringId = String.format(DEFAULT_STRING_ID, parentId, subParentId);
                break;
            case TreasureItemType.STORY_REFLECTION:
                stringId =  String.format(STORY_REFLECTION_STRING_ID, parentId, subParentId);
                break;
            case TreasureItemType.CALMING_PROMPT:
                stringId =  String.format(RESOLUTION_STRING_ID, parentId, subParentId);
                break;
            default:
                stringId =  String.format(DEFAULT_STRING_ID, parentId, subParentId);
                break;
        }
        return String.format(ITERATION_STRING_ID, iteration, stringId);
    }
}
