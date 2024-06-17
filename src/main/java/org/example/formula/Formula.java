package org.example.formula;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Formula {
    //разделитель между параметрами в формуле
    protected static final String PARAM_SPLIT = "~";
    //разделитель между названием параметра и его значением
    protected static final String TITLE_SPLIT = "->";

    public Formula(){
    }

    //TODO: для выпадающего списка посмотреть возможность выводить имеющиеся в бд поля
    //откуда брать данные - ОБЯЗАТЕЛЬНЫЕ поля
    protected String url = null;
    protected String username = null;
    protected String password = null;

    protected String table = null;
    protected String column = null;
    protected String primaryKey = null;
    protected String primaryKeyValue = null;

    //сохранять ли старую стилизацию - по умолчанию нет
    protected Boolean saveOldStyle = false;

    //стилизация - по умолчанию нет
    protected Boolean isCursive = false;//курсив
    protected Boolean isBald = false;//выделен жирным
    protected Boolean isUnderlined = false;//подчеркивание текста
    protected String highlighting = null;//желтые выделения   "Yellow", etc. "absent" - без выделения
    protected String color = null;//цвет текста  "Red", etc


    //шрифты, размеры - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
    protected String font = null;//название шрифта "Times New Roman", etc
    protected Integer fontSize = null;//размер шрифта 12, etc

    //форматирование абзаца - ОБЯЗАТЕЛЬНО, если не указано сохранять стилизацию
//    PPrBase.Ind ind = factory.createPPrBaseInd();
//    ind.setFirstLine(new BigInteger("709"));
//    pPr.setInd(ind);
    //protected static Boolean hasIndent = false;//есть ли отступ в начале нового параграфа
    //остальные свойства параграфа типа текст по ширине


    public void setOldStyle(Boolean saveOldStyle){
        this.saveOldStyle = saveOldStyle;
    }

    public void setDatabaseParams(String url, String username, String password,
                                  String table, String column, String primaryKey, String primaryKeyValue){
        if (!table.isBlank() && !column.isBlank() && !primaryKey.isBlank()
                && !url.isBlank()) {

            this.table = table;
            this.column = column;
            this.primaryKey = primaryKey;
            this.url = url;
            this.username = username;
            this.password = password;
            this.primaryKeyValue = primaryKeyValue;
        } else
            throw new IllegalArgumentException("Incorrect database params");
    }

    public void setStyle(Boolean isCursive, Boolean isBald, Boolean isUnderlined, String highlighting, String color){
        if(isCursive != null && isUnderlined != null && isBald != null && highlighting != null && color != null) {
            this.isCursive = isCursive;//курсив
            this.isBald = isBald;//выделен жирным
            this.isUnderlined = isUnderlined;
            this.highlighting = highlighting;//желтые выделения
            this.color = color;//цвет текста
        } else
            throw new IllegalArgumentException("At least one of arguments is null");
    }

    public void setFont(String font, Integer fontSize){
        this.font = font;//название шрифта "Times New Roman", etc
        this.fontSize = fontSize;
    }

    /**
     * Метод проверяет, заданы ли обязательные параметры
     * @return true - заданы, false - нет
     */
    public Boolean checkMandatoryParams(){
        //база данных в любом случае должна быть задана(имя и пароль могут быть пустыми)
        if(table.isBlank() || column.isBlank() || primaryKey.isBlank()
                || url.isBlank() || primaryKeyValue.isBlank())
            return false;
        //если сохраняется старая стилизация, то необходимо проверить только задание базы данных
        if(saveOldStyle){
            return true;
        }else{
            //проверяем все обязательные поля
            if(font == null || fontSize == null || highlighting == null || color == null)
                return false;
            else
                return true;
        }
    }

    /**
     * Метод для проверки того, что для подстановки значения в 2 формулы, нужен один Connection
     * @param formula вторая формула
     * @return true - имеет одинаковые параметры, false - нет
     */
    public Boolean hasEqualDatabaseConnection(Formula formula){
        return this.url.equals(formula.url) && this.username.equals(formula.username) && this.password.equals(formula.password);
    }
}
