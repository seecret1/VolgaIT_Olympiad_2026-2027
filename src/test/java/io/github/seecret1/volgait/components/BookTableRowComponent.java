package io.github.seecret1.volgait.components;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;

/** Locators are resolved relative to one book row, not the entire page. */
public final class BookTableRowComponent {
    @FindBy(css = "a[href*='search=']")
    private WebElement link;

    @FindBy(css = "img")
    private WebElement image;

    @FindBy(css = "span[title='Delete']")
    private WebElement deleteButton;

    public BookTableRowComponent(WebElement row) {
        PageFactory.initElements(new DefaultElementLocatorFactory(row), this);
    }

    public String title() {
        return link.getText().trim();
    }

    public String href() {
        return link.getAttribute("href");
    }

    public String imageSource() {
        return image.getAttribute("src");
    }

    public WebElement deleteButton() {
        return deleteButton;
    }
}
