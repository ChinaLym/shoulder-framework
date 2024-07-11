package org.shoulder.core.dictionary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.dictionary.convert.DictionaryItemConversions;
import org.shoulder.core.dictionary.convert.DictionaryItemEnumSerialGenericConverter;
import org.shoulder.core.dictionary.convert.DictionaryItemToStrGenericConverter;
import org.shoulder.core.dictionary.convert.ToDictionaryEnumGenericConverter;
import org.shoulder.core.dictionary.model.DictionaryItem;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.dictionary.spi.DefaultDictionaryEnumStore;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.ArrayUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.util.HashMap;

/**
 * DictionaryEnumStoreTest 单测
 *
 * @author lym
 */
public class DictionaryTest {

    private static boolean ignoreCase = true;

    private static DefaultDictionaryEnumStore dictionaryEnumStore = new DefaultDictionaryEnumStore(ignoreCase);

    @Test
    public void testDefaultDictionaryEnumStore() {
        Assertions.assertEquals(ignoreCase, dictionaryEnumStore.isIgnoreCase());

        Assertions.assertFalse(dictionaryEnumStore.contains(ColorStrEnum.class));
        Assertions.assertThrows(BaseRuntimeException.class, () -> dictionaryEnumStore.list(ColorStrEnum.class));
        Assertions.assertThrows(BaseRuntimeException.class, () -> dictionaryEnumStore.listAllAsDictionaryEnum(ColorStrEnum.class.getSimpleName()));

        // 注册
        dictionaryEnumStore.register(ColorStrEnum.class);

        Assertions.assertTrue(dictionaryEnumStore.contains(ColorStrEnum.class));
        Assertions.assertEquals(dictionaryEnumStore.list(ColorStrEnum.class).size(), ColorStrEnum.values().length);
        Assertions.assertEquals(dictionaryEnumStore.listAllAsDictionaryEnum(ColorStrEnum.class.getSimpleName()).size(), ColorStrEnum.values().length);
        Assertions.assertEquals(dictionaryEnumStore.listAllAsDictionaryEnum(ColorStrEnum.class.getSimpleName().toLowerCase()).size(), ColorStrEnum.values().length);

        Assertions.assertEquals(dictionaryEnumStore.listAllTypes().toArray()[0], ColorStrEnum.class);
        Assertions.assertEquals(dictionaryEnumStore.listAllTypeNames().toArray()[0], ColorStrEnum.class.getSimpleName().toLowerCase());

        Assertions.assertEquals(dictionaryEnumStore.getActuallyType(ColorStrEnum.class.getSimpleName()), ColorStrEnum.class);
        Assertions.assertEquals(dictionaryEnumStore.getActuallyType(ColorStrEnum.class.getSimpleName().toLowerCase()), ColorStrEnum.class);
        Assertions.assertEquals(dictionaryEnumStore.mapToStorageKey(ColorStrEnum.class), ColorStrEnum.class.getSimpleName().toLowerCase());

        // 清理
        dictionaryEnumStore.remove(ColorStrEnum.class);
        Assertions.assertFalse(dictionaryEnumStore.contains(ColorStrEnum.class));
        Assertions.assertThrows(BaseRuntimeException.class, () -> dictionaryEnumStore.list(ColorStrEnum.class));

        ignoreCase = false;
        dictionaryEnumStore = new DefaultDictionaryEnumStore(ignoreCase);
        dictionaryEnumStore.register(ColorStrEnum.class);
        Assertions.assertEquals(dictionaryEnumStore.listAllAsDictionaryEnum(ColorStrEnum.class.getSimpleName()).size(), ColorStrEnum.values().length);
        Assertions.assertEquals(dictionaryEnumStore.listAllTypes().toArray()[0], ColorStrEnum.class);
        Assertions.assertEquals(dictionaryEnumStore.listAllTypeNames().toArray()[0], ColorStrEnum.class.getSimpleName());
        Assertions.assertEquals(dictionaryEnumStore.getActuallyType(ColorStrEnum.class.getSimpleName()), ColorStrEnum.class);
        Assertions.assertEquals(dictionaryEnumStore.mapToStorageKey(ColorStrEnum.class), ColorStrEnum.class.getSimpleName());
    }

    @Test
    public void testBasicModel() {
        HashMap<String, String> condition = new HashMap<>();
        condition.put("color", ColorStrEnum.WHITE.name());
        Assertions.assertTrue(ColorStrEnum.WHITE.matchCondition("color", ColorStrEnum.WHITE.name()));
        Assertions.assertTrue(ColorStrEnum.WHITE.matchAllCondition(condition));

        Assertions.assertFalse(ColorStrEnum.WHITE.matchCondition("color", "black"));

        // -------

        Assertions.assertEquals(ColorStrEnum.WHITE, DictionaryItemEnum.fromId(ColorStrEnum.class, ColorStrEnum.WHITE.name()));
        Assertions.assertEquals(ColorStrEnum.WHITE, DictionaryItemEnum.fromName(ColorStrEnum.class, ColorStrEnum.WHITE.name()));
        Assertions.assertEquals(ColorStrEnum.WHITE, DictionaryItemEnum.fromEnumCodingName(ColorStrEnum.class, ColorStrEnum.WHITE.name()));
        Assertions.assertEquals(ColorStrEnum.WHITE, DictionaryItemEnum.fromOrder(ColorStrEnum.class, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> DictionaryItemEnum.fromName(ColorStrEnum.class, "NONE"));

        Assertions.assertTrue(ArrayUtils.equals(ColorStrEnum.class.getEnumConstants(), DictionaryItemEnum.values(ColorStrEnum.class)));
        Assertions.assertEquals(String.class, DictionaryItemEnum.resovleEnumItemIdClass(ColorStrEnum.class));

        Assertions.assertEquals(String.class, ColorStrEnum.WHITE.getEnumItemIdClass());
        Assertions.assertEquals(ColorStrEnum.class.getSimpleName(), ColorStrEnum.WHITE.getDictionaryType());
        Assertions.assertEquals(0, ColorStrEnum.WHITE.getDisplayOrder());

    }


    @Test
    public void testConvertDictionaryItemConversions() {
        DictionaryItem item = DictionaryItemConversions.toItem(ColorStrEnum.WHITE.name(), ColorStrEnum.class);
        Assertions.assertNotNull(item);
        Assertions.assertEquals(DictionaryItemEnum.resovleEnumItemIdClass(item.getClass()), DictionaryItemEnum.resovleEnumItemIdClass(ColorStrEnum.class));
        Assertions.assertEquals(item.getName(), ColorStrEnum.WHITE.getName());
        Assertions.assertEquals(item.getItemId(), ColorStrEnum.WHITE.getItemId());
        Assertions.assertEquals(item.getDisplayName(), ColorStrEnum.WHITE.getDisplayName());
        Assertions.assertEquals(item.getDisplayOrder(), ColorStrEnum.WHITE.getDisplayOrder());
        Assertions.assertNull(item.getDescription());

        String str = DictionaryItemConversions.toStr(ColorStrEnum.WHITE);
        Assertions.assertEquals(str, ColorStrEnum.WHITE.getName());
        String str2 = DictionaryItemConversions.toStr(ColorIntEnum.GRAY);
        Assertions.assertEquals(str2, ColorIntEnum.GRAY.name());

        String str3 = DictionaryItemConversions.toStr(null);
        Assertions.assertEquals(str3, null);
    }


    @Test
    public void testDictionaryItemToStrGenericConverter() {
        DictionaryItemToStrGenericConverter.INSTANCE.getConvertibleTypes();

        TypeDescriptor sourceType = TypeDescriptor.valueOf(ColorStrEnum.class);
        TypeDescriptor targetType = TypeDescriptor.valueOf(Integer.class);

        // 不支持枚举
        Assertions.assertFalse(DictionaryItemToStrGenericConverter.INSTANCE.matches(sourceType, targetType));

        sourceType = TypeDescriptor.valueOf(MyTestDictionary.class);
        MyTestDictionary source = new MyTestDictionary();
        source.setItemId("id");
        source.setName("name");
        source.setDictionaryType("someType");
        source.setDisplayOrder(66);

        Assertions.assertNull(DictionaryItemToStrGenericConverter.INSTANCE.convert(null, sourceType, targetType));
        Assertions.assertEquals(source.getItemId(), DictionaryItemToStrGenericConverter.INSTANCE.convert(source, sourceType, targetType));

        Assertions.assertEquals(source.getItemId(), DictionaryItemConversions.toStr(source));


    }

    @Test
    public void testDictionaryItemEnumSerialGenericConverter() {
        DictionaryItemEnumSerialGenericConverter.INSTANCE.getConvertibleTypes();
        TypeDescriptor sourceType;
        TypeDescriptor targetType;

        ColorStrEnum strEnumSource = ColorStrEnum.RED;
        ColorIntEnum intEnumSource = ColorIntEnum.GRAY;

        // strEnum -> int
        sourceType = TypeDescriptor.valueOf(ColorStrEnum.class);
        targetType = TypeDescriptor.valueOf(Integer.class);
        Assertions.assertTrue(DictionaryItemEnumSerialGenericConverter.INSTANCE.matches(sourceType, targetType));
        Assertions.assertNull(DictionaryItemEnumSerialGenericConverter.INSTANCE.convert(null, sourceType, targetType));

        Assertions.assertEquals(strEnumSource.ordinal(),
            DictionaryItemEnumSerialGenericConverter.INSTANCE.convert(strEnumSource, sourceType, targetType));

        // strEnum -> str
        targetType = TypeDescriptor.valueOf(String.class);
        Assertions.assertEquals(strEnumSource.getItemId(),
            DictionaryItemEnumSerialGenericConverter.INSTANCE.convert(strEnumSource, sourceType, targetType));

        // intEnum -> str
        sourceType = TypeDescriptor.valueOf(ColorIntEnum.class);
        Assertions.assertEquals(intEnumSource.name(),
            DictionaryItemEnumSerialGenericConverter.INSTANCE.convert(intEnumSource, sourceType, targetType));

        // intEnum -> int
        targetType = TypeDescriptor.valueOf(Integer.class);
        Assertions.assertEquals(intEnumSource.getItemId(),
            DictionaryItemEnumSerialGenericConverter.INSTANCE.convert(intEnumSource, sourceType, targetType));
    }

    @Test
    public void testToDictionaryEnumGenericConverter() {
        ToDictionaryEnumGenericConverter.INSTANCE.getConvertibleTypes();
        TypeDescriptor sourceType;
        TypeDescriptor targetType;

        // int -> intEnum
        sourceType = TypeDescriptor.valueOf(Integer.class);
        targetType = TypeDescriptor.valueOf(ColorIntEnum.class);
        Assertions.assertTrue(ToDictionaryEnumGenericConverter.INSTANCE.matches(sourceType, targetType));
        Assertions.assertNull(ToDictionaryEnumGenericConverter.INSTANCE.convert(null, sourceType, targetType));

        Assertions.assertEquals(ColorIntEnum.GRAY,
            ToDictionaryEnumGenericConverter.INSTANCE.convert(ColorIntEnum.GRAY.getItemId(), sourceType, targetType));

        // int -> strEnum
        targetType = TypeDescriptor.valueOf(ColorStrEnum.class);
        Assertions.assertEquals(ColorStrEnum.BLUE,
            ToDictionaryEnumGenericConverter.INSTANCE.convert(ColorStrEnum.BLUE.ordinal(), sourceType, targetType));

        // str -> strEnum
        sourceType = TypeDescriptor.valueOf(String.class);
        Assertions.assertEquals(ColorStrEnum.BLUE,
            ToDictionaryEnumGenericConverter.INSTANCE.convert(ColorStrEnum.BLUE.getItemId(), sourceType, targetType));

        // str -> intEnum
        targetType = TypeDescriptor.valueOf(ColorIntEnum.class);
        Assertions.assertEquals(ColorIntEnum.GRAY,
            ToDictionaryEnumGenericConverter.INSTANCE.convert(ColorIntEnum.GRAY.name(), sourceType, targetType));
        Assertions.assertEquals(ColorIntEnum.GRAY,
            ToDictionaryEnumGenericConverter.INSTANCE.convert(String.valueOf(ColorIntEnum.GRAY.getItemId()), sourceType, targetType));


    }


}
