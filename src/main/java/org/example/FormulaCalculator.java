package org.example;

import lombok.Data;
import org.docx4j.wml.PPrBase;

public class FormulaCalculator {
    private static final String SPLIT = "#";
    private FormulaCalculator(){
    }

    //сохранять ли старую стилизацию - по умолчанию нет
    private Boolean saveOldStyle = false;

    //TODO: для выпадающего списка посмотреть возможность выводить имеющиеся в бд поля
    //откуда брать данные - ОБЯЗАТЕЛЬНЫЕ поля
    private String database = null;
    private String table = null;
    private String field = null;
    private String primaryKey = null;

    //форматирование - по умолчанию нет
    private Boolean isHeader = null;//является ли текст заголовком

    //стилизация - по умолчанию нет
    private Boolean isCursive = null;//курсив
    private Boolean isBald = null;//выделен жирным
    //TODO: валидация введенного цвета
    //public static String highlighting = null;//желтые выделения   "Yellow", etc
    //public static String color = null;//цвет текста  "Red", etc


    //шрифты, размеры - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
    private String font = null;//название шрифта "Times New Roman", etc
    private Integer fontSize = null;//размер шрифта 12, etc

    //форматирование абзаца - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
//    PPrBase.Ind ind = factory.createPPrBaseInd();
//    ind.setFirstLine(new BigInteger("709"));
//    pPr.setInd(ind);
    //private static Boolean hasIndent = false;//есть ли отступ в начале нового параграфа
    //остальные свойства параграфа типа текст по ширине


    public void setOldStyle(Boolean saveOldStyle){
        this.saveOldStyle = saveOldStyle;
    }

    public void setDatabaseParams(String database, String table, String field, String primaryKey){
        if (!database.isBlank() && !table.isBlank() && !field.isBlank() && !primaryKey.isBlank()) {
            this.database = database;
            this.table = table;
            this.field = field;
            this.primaryKey = primaryKey;
        } else
            throw new IllegalArgumentException("Incorrect database params");
    }

    public void setFormatting(Boolean isHeader){
        this.isHeader = isHeader;
    }

    public void setStyle(Boolean isCursive, Boolean isBald){
        this.isCursive = null;//курсив
        this.isBald = null;//выделен жирным
    }

    public void setFont(String font, Integer fontSize){
        this.font = null;//название шрифта "Times New Roman", etc
        this.fontSize = null;
    }

    /**
     * Метод для вычисления формулы для подстановки закладки
     * @return вычисленная строка
     */
    public String calculate() {
        //проверка, заданы ли обязательные параметры
        if (checkMandatoryParams()){
            //если нужно оставить старую стилизацию
            if (saveOldStyle)
                return calculateDatabaseString() + SPLIT + "old";
            else  {
                return calculateDatabaseString() + SPLIT + calculateFontString() + SPLIT +
                        calculateFormattingString() + SPLIT + calculateStyleString();
            }
        } else throw new IllegalStateException("Not all required parameters are set");
    }

    /**
     * Метод проверяет, заданы ли обязательные параметры для создания формулы
     * @return true - заданы, false - нет
     */
    private Boolean checkMandatoryParams(){
        //база данных в любом случае должна быть задана
        if(database.isBlank() || table.isBlank() || field.isBlank() || primaryKey.isBlank())
            return false;
        //если сохраняется старая стилизация, то необходимо проверить только задание базы данных
        if(saveOldStyle){
            return true;
        }else{
            //проверяем все обязательные поля
            //TODO: добавить остальные поля
            if((font.isBlank() || fontSize == null))
                return false;
            else
                return true;
        }
    }

    /**
     * Метод вычисляет строку с параметрами БД для постановки в формулу
     * @return вычисленная строка
     */
    private String calculateDatabaseString(){
        return database + SPLIT + table + SPLIT + field + SPLIT + primaryKey;
    }

    /**
     * Метод вычисляет строку с параметрами форматирования для постановки в формулу
     * @return вычисленная строка
     */
    private String calculateFormattingString(){
        return isHeader.toString();
    }

    /**
     * Метод вычисляет строку с параметрами стилизации для постановки в формулу
     * @return вычисленная строка
     */
    private String calculateStyleString(){
        return isCursive.toString() + SPLIT + isBald.toString();
    }

    /**
     * Метод вычисляет строку с параметрами шрифта для постановки в формулу
     * @return вычисленная строка
     */
    private String calculateFontString(){
        return font + SPLIT + fontSize.toString();
    }
}