insert into product (id, name) values (1, 'Product 1');
insert into product (id, name) values (2, 'Product 2');
insert into product (id, name) values (3, 'Product 3');

insert into inventory (id, product_id, on_hand_quantity, reserved_quantity) values (1, 1, 3, 0);
insert into inventory (id, product_id, on_hand_quantity, reserved_quantity) values (2, 2, 4, 0);
insert into inventory (id, product_id, on_hand_quantity, reserved_quantity) values (3, 3, 5, 0);

-- insert into orders (id) values (1);
-- insert into orders (id) values (2);
--
-- insert into order_items (id, order_id, product_id, quantity) values (1, 1, 1, 2);
-- insert into order_items (id, order_id, product_id, quantity) values (2, 1, 2, 1);
-- insert into order_items (id, order_id, product_id, quantity) values (3, 2, 3, 4);