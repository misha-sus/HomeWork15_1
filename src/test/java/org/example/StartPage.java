package org.example;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StartPage {

    private static RemoteWebDriver driver;
    private static WebElement element;
    public static List<String> products = new ArrayList<>();
    private int sizeBasket;
    private Logger log = LogManager.getRootLogger();

    public StartPage() {
        log.debug("Инициализирую экземпляр обертки над веб драйвером.");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        System.setProperty("webdriver.chrome.driver", "/Users/mihailhusvahtov/Downloads/chromedriver ");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        log.debug("Инициализация обретки завершина.");
    }

    //открывает сайт регард
    public void open() {
        driver.get("https://www.regard.ru/");
        log.info("Перешли на главную страницу https://www.regard.ru/");
    }

    //нажать на элемент
    public void click(WebElement elementClick) {
        elementClick.click();
        log.debug("Нажимаем на кнопку по xpath '{}'", elementClick);
    }

    public WebElement findElement(By xpath) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 5, 250);
            log.debug("Находим нужную кнопку по локатору '{}'", xpath);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(xpath));
            driver.executeScript("arguments[0].scrollIntoView(true);", element);
            ((JavascriptExecutor) driver).executeScript("arguments[0]['style']['backgroundColor'] = 'darksalmon';", element);
            return element;
        } catch (RuntimeException e) {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String newFileName = String.format(
                    "%s\\screenshots\\%s.png", System.getProperty("user.dir"), LocalDateTime.now().toString().replace(":", "-"));
            File destination = new File(newFileName);
            try {
                FileUtils.copyFile(source, destination);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            driver.quit();
            throw e; }
    }
    //выбираем категорию и подкатегорию в меню
    public void selectMenuCategory(String searchProducts, String search) {
        element = findElement((By.xpath(String.format("//*[text() = '%s']", searchProducts))));
        log.info("Выбираем категорию  в меню");
        click(element);

        //подкатегорию
        element = findElement(By.xpath
                (String.format("//*[@id='lmenu']/ul[@class = 'menu ']/li[a[text () = '%s']]/ul/li[a[text()='%s']]/a", searchProducts, search)));
        log.info("Выбираем подкатегорию  в меню");
        click(element);
        assertNotNull(element, "Отсутствует подкатегория в меню");
    }

    //поиск товара под  отпределенным номером в списке и добавлению в корзину
    public void searchInList(int numberList) {
        log.debug("Поиск товара под  отпределенным номером в списке и добавлению в корзину");
        WebElement addToBasket = findElement((By.xpath(String.format("//div[@class = 'block'][%d]//a[@class = 'cart']", numberList))));
        products.add(getIdProduct(numberList));
        log.info("Выбираем товар по номеру в списке");
        click(element);
        assertNotNull(addToBasket, "Под таким номером в списке нет товара");
    }

    //поиск по номеру и переход по ссылки-названию
    public void searchInList(int numberList, String header) {
        log.debug("Поиск по номеру и переход по ссылки-названию");

        element = findElement((By.xpath(String.format("//div[@class = 'block'][%d]//a[@class = '%s']", numberList, header))));
        products.add(getIdProduct(numberList));
        click(element);
        assertNotNull(element, "Под таким номером в списке нет товара");
    }

    //добавление в корзину и переход в саму корзину
    public void addAndOpenToBasket(String xpath) {
        log.debug("Добавление в корзину и переход в саму корзину");
        element = findElement((By.xpath(String.format("//div[@id = 'cart_btn']/a[@title = '%s']", xpath))));
        click(element);
        assertNotNull(element, "Отсутствует левый блок меню!");
    }


    //получаем id добавленного продукта
    public String getIdProduct(int num) {
        log.debug("Получаем id добавленного продукта");
        String idProduct = driver.findElement(
                By.xpath(String.format("//div[@class = 'block'][%d]/*//div[@class = 'code']", num))).getText();
        log.debug("Получали id добавленного продукта");
        return idProduct;
    }

    //Получает id товаров в корзине
    public List<String> getIdProductsInBasket() {
        log.debug("Добавляем в свой список те продукты которые добавили в корзину, для проверки");
        List<String> getBasket = new ArrayList<>();
        for (int i = 3; i < sizeBasket + 3; i++) {
            getBasket.add(driver.findElement(
                    By.xpath(String.format("//*[@id='table-basket']/tbody/tr[%d]/td[@class = 't1']", i))).getText());
        }
        return getBasket;
    }
    public  int sizeBasket() {
        sizeBasket = driver.findElements(By.xpath("//*[@id='table-basket']/tbody/tr[@data-groupid]")).size();
        assertTrue(sizeBasket > 0);
        log.debug("Узнали размер корзины = {}", sizeBasket);
        return sizeBasket;
    }
    //проверяет совпадает ли корзина с товарами которые мы добавили
    public void checksBasket() {
        log.debug("Сравниваем совпадает ли корзина с добавленными продуктами");
        for (int i = 0; i < 3; i++)
            assertTrue(StartPage.products.get(i).contains(getIdProductsInBasket().get(i)));
        log.debug("Корзина совпадает с добавленными продуктами");

    }

    public void close() {
        log.info("Закрываем браузер полностью");
        driver.quit();
        log.info("Закрыли браузер полностью");
    }
}
