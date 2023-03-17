package com.github.pingia;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@ContentRowHeight(30)
@HeadRowHeight(20)


public class AndroidStringData {
    @ExcelProperty("字符串名")
    @ColumnWidth(40)
    private String name;

    @ExcelProperty("字符串值")
    @ColumnWidth(100)
    private String value;


    @ExcelProperty(index = 2,value="翻译值")
    @ColumnWidth(100)
    private String translationValue;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTranslationValue() {
        return translationValue;
    }

    public void setTranslationValue(String translationValue) {
        this.translationValue = translationValue;
    }
}
