import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static com.codeborne.selenide.Selenide.*;

public class SelenideWebTest {

    @BeforeAll
    static void setup() {

        Configuration.baseUrl = "https://qaplayground.dev";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
    }

    @AfterEach
    void tearDown() {
        Selenide.closeWebDriver();
    }

    @ValueSource(strings = {
            "Java", "C++", "C#", "Python", "PHP"
    })
    @ParameterizedTest(name = "Заполнить тэги: {0}")
    @Tag("Value Source")
    void selenideShouldFillTheTagsTest(String tags){
        open("/apps/tags-input-box/");
        $("input[type='text'][spellcheck='false']").setValue(tags).pressEnter();
        $$(".content ul li").shouldHave(CollectionCondition.itemWithText(tags));
        System.out.println("I've typed new tag name " + tags);
    }

    @CsvSource(value = {
            "1, I just hate it",
            "2, I don't like it",
            "3, This is awesome",
            "4, I just like it",
            "5, I just love it"
    })
    @ParameterizedTest(name = "Поставить звездочку и проверить получаемый текст, рейтинг {0}")
    @Tag("CSV Source")
    void clickRatingTest(int rate, String expectedText){
        // Открываем страницу
        open("/apps/rating/");
        // Кликаем по звездочке
        $$(".stars label").get(rate - 1).click();
        // Проверяем, что в блоке с текстом появился ожидания отклик

        // Извлекаем значение CSS-свойства content у псевдоэлемента ::before
        String actualText = Selenide.executeJavaScript(
                "return window.getComputedStyle(document.querySelector('.text'), '::before').getPropertyValue('content');"
        );
        // CSS возвращает текст в кавычках (например, "\"I just hate it\""), поэтому убираем кавычки:
        actualText = actualText.replace("\"", "");

        Assertions.assertEquals(expectedText, actualText);
    }

    @CsvFileSource(resources = "/test_data/BudgetTrackerTest.csv", numLinesToSkip = 1)
    @ParameterizedTest(name = "Заполнить таблицы с расходами за сегодня {0}")
    @Tag("CSV File Source")
    void BudgetTrackerTest(String description, int amount){
        //открываем нужную страницу на сайте
        open("/apps/budget-tracker/");
        //кликаем на кнопку чтобы доавбить новую запись
        $(".new-entry").click();
        // Находим ПОСЛЕДНИЕ появившиеся поля (так как строки добавляются)
        $$("input[placeholder*='Description']").last().setValue(description);
        $$("select").last().selectOption("Expense"); // выбираем Расход
        $$("input[type='number']").last().setValue(String.valueOf(amount)).pressEnter();

        // Посчитать сумму всех заполненого Amount через Stream API
        int expectedSum = $$("input[type='number']").stream()
                .map(input -> input.getValue())
                .filter(val -> val != null && !val.isEmpty())
                .mapToInt(Integer::parseInt)
                .sum();
        // Формируем ожидаемую строку для assert, так как это Expenses то Total будет со знаком минус
        String expectedTotalString = String.format("-$%,d.00", expectedSum);
        String actualTotalString = $(".total").getText();

        System.out.println("Expected total string " + expectedTotalString + " и Actual total string " + actualTotalString);
        Assertions.assertEquals(expectedTotalString, actualTotalString, "Итоговая сумма Total не совпадает с ожидаемой!");
    }
}
