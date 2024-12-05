package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

/**
 * Тестирование класса {@link ShoppingServiceImpl}
 */
class ShoppingServiceImplTest {

    private final ProductDao productDaoMock = Mockito.mock(ProductDao.class);
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDaoMock);

    /**
     * Тестировать получение одних и тех же объектов корзины из метода getCart
     * (тест упадет, так как при каждом вызове метода создается новый объект)
     */
    @Test
    public void testGetEqualsCarts() {
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart1 = shoppingService.getCart(customer);
        Cart cart2 = shoppingService.getCart(customer);

        Assertions.assertEquals(cart1, cart2);
    }

    @Test
    public void testGetAllProducts() {
        /*
        Тестировать метод нет необходимости, так как он только возвращает
        все товары из базы данных. Метод не хранит в себе логики, которую можно
        протестировать. Вся работа происходит внутри другого класса ProductDao
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
     * Тестировать проверку покупки на пустой корзине
     */
    @Test
    public void testEmptyCart() throws BuyException {
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart = new Cart(customer);

        Assertions.assertFalse(shoppingService.buy(cart));
    }

    /**
     * Тестировать весь процесс покупки из корзины, включая уменьшение количества
     * товаров, их сохранение в базу данных и очищение корзины
     * (тест упадет, так как после покупки корзина не очищается)
     * @throws BuyException невозможность совершить покупку
     */
    @Test
    public void testBuyProcess() throws BuyException {
        Product product1 = new Product("name1", 3);
        Product product2 = new Product("name2", 2);
        Customer customer = new Customer(1L, "11-11-11");

        Cart cart = new Cart(customer);
        cart.add(product1, 2);
        cart.add(product2, 1);
        shoppingService.buy(cart);

        Assertions.assertEquals(1, product1.getCount());
        Assertions.assertEquals(1, product2.getCount());

        Mockito.verify(productDaoMock)
                .save(Mockito.argThat(product -> product.getName().equals("name1")
                        && product.getCount() == product1.getCount()));
        Mockito.verify(productDaoMock)
                .save(Mockito.argThat(product -> product.getName().equals("name2")
                        && product.getCount() == product1.getCount()));

        Assertions.assertTrue(cart.getProducts().isEmpty());
    }

    /**
     * Тестировать корректное уменьшение количества товаров, после их покупки
     * (тест упадет, так как метод validateCount класса Cart сравнивает
     * разницу количества товаров и количества товаров в корзине между нулем и меньшими значениями.
     * При таком условии нельзя будет купить ровно столько товаров, сколько имеется в наличии)
     * @throws BuyException невозможность совершить покупку
     */
    @Test
    public void testCorrectSubtractCountProduct() throws BuyException {
        Product product = new Product("name", 3);
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart = new Cart(customer);
        cart.add(product, 3);
        shoppingService.buy(cart);

        Assertions.assertEquals(0, product.getCount());
    }

    /**
     * Тестировать некорректное количество товаров в корзине и товарах
     * (количество товаров в корзине больше, чем фактическое их количество)
     */
    @Test
    public void testIncorrectSubtractCountProduct() {
        Product product = new Product("name", 3);
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart = new Cart(customer);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                cart.add(product, 4));

        Assertions.assertEquals("Невозможно добавить товар 'name' в корзину," +
                " т.к. нет необходимого количества товаров",
                exception.getMessage());
    }

    /**
     * Тестировать отрицательное значение количества товаров
     * (тест упадет, так как у корзины нет проверки на отрицательные числа)
     */
    @Test
    public void testNegativeCountOfProduct() {
        Product product = new Product("name", -3);
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart = new Cart(customer);

        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                cart.add(product, 2));

        Assertions.assertEquals("Отрицательное количество товаров",
                exception.getMessage());
    }

    /**
     * Тестировать покупку товара, который закончился
     */
    @Test
    public void testBuyOutOfStockProduct() throws BuyException {
        Product product = new Product("name", 3);
        Customer customer = new Customer(1L, "11-11-11");
        Cart cart = new Cart(customer);

        cart.add(product, 2);
        shoppingService.buy(cart);

        product.subtractCount(1);
        Assertions.assertEquals(0, product.getCount());

        BuyException exception = Assertions.assertThrows(BuyException.class, () ->
                shoppingService.buy(cart));

        Assertions.assertEquals("В наличии нет необходимого количества товара 'name'",
                exception.getMessage());
    }
}