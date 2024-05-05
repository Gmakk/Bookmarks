package org.example.formula;

public class FormulaCalculator extends Formula {
    //Поля в формуле должны быть в том же порядке, что и в классе Formula
    public FormulaCalculator(){
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
                return calculateDatabaseString() + SPLIT + calculateOldStyleString();
            else  {
                return calculateDatabaseString() + SPLIT + calculateOldStyleString() + SPLIT+ calculateFormattingString() + SPLIT +
                        calculateStyleString() + SPLIT + calculateFontString();
            }
        } else throw new IllegalStateException("Not all required parameters are set");
    }

    /**
     * Метод проверяет, заданы ли обязательные параметры для создания формулы
     * @return true - заданы, false - нет
     */
    private Boolean checkMandatoryParams(){
        //база данных в любом случае должна быть задана
        if(database.isBlank() || table.isBlank() || column.isBlank() || primaryKey.isBlank())
            return false;
        //если сохраняется старая стилизация, то необходимо проверить только задание базы данных
        if(saveOldStyle){
            return true;
        }else{
            //проверяем все обязательные поля
            //TODO: добавить остальные поля
            if(font.isBlank() || fontSize == null || highlighting == null || color == null)
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
        return database + SPLIT + table + SPLIT + column + SPLIT + primaryKey;
    }

    /**
     * Метод вычисляет строку с параметрами БД для постановки в формулу
     * @return вычисленная строка
     */
    private String calculateOldStyleString(){
        return saveOldStyle.toString();
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
        return isCursive.toString() + SPLIT + isBald.toString() + SPLIT + highlighting + SPLIT + color;
    }

    /**
     * Метод вычисляет строку с параметрами шрифта для постановки в формулу
     * @return вычисленная строка
     */
    private String calculateFontString(){
        return font + SPLIT + fontSize.toString();
    }
}