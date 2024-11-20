package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Тестирование класса {@link ShoppingServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
class ShoppingServiceImplTest {

    private final ProductDao productDaoMock = Mockito.mock(ProductDao.class);
    private final Cart cartMock = Mockito.mock(Cart.class);
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDaoMock);

    @Test
    public void testGetCart() {
        /*
        Тестировать метод нет необходимости, так как он лишь возвращает
        объект класса Cart
         */
    }

    @Test
    public void testGetAllProducts() {
        /*
        Тестировать метод нет необходимости, так как он только возвращает
        все товары из базы данных
         */
    }

    @Test
    public void testGetProductByName() {
        /*
        Тестировать метод нет необходимости, так как он только возвращает
        товар по его имени из базы данных. В нем нет никаких проверок на это имя
        и существования товара в базе данных
         */
    }

    /**
     * Тестировать проверку на пустую корзину
     */
    @Test
    public void testEmptyCart() throws BuyException {
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart = new Cart(customer);

        Assertions.assertFalse(shoppingService.buy(cart));
    }

    /**
     * Тестировать сохранение товара, если корзина не пуста, в базу данных
     */
    @Test
    public void testBuy() throws BuyException {
        Product product1 = new Product("name1", 2);
        Product product2 = new Product("name2", 3);
        HashMap<Product, Integer> products = new LinkedHashMap<>();
        products.put(product1, 2);
        products.put(product2, 1);

        Mockito.when(cartMock.getProducts())
                .thenReturn(products);

        Assertions.assertTrue(shoppingService.buy(cartMock));

        Mockito.verify(productDaoMock, Mockito.times(1))
                .save(Mockito.eq(product1));
        Mockito.verify(productDaoMock, Mockito.times(1))
                .save(Mockito.eq(product2));
    }

    /**
     * Тестировать получение ошибки BuyException
     */
    @Test
    public void testBuyException() {
        Product product1 = new Product("name1", 3);
        Product product2 = new Product("name2", 1);
        Map<Product, Integer> products = new HashMap<>();
        products.put(product1, 2);
        products.put(product2, 2);

        Mockito.when(cartMock.getProducts())
                .thenReturn(products);

        Exception exception = Assertions.assertThrows(BuyException.class, () ->
                shoppingService.buy(cartMock));

        Assertions.assertEquals("В наличии нет необходимого количества товара 'name2'",
                exception.getMessage());
    }
}