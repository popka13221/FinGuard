INSERT INTO categories (user_id, name, type)
SELECT NULL, 'Еда', 'EXPENSE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE user_id IS NULL AND name = 'Еда'
);

INSERT INTO categories (user_id, name, type)
SELECT NULL, 'Транспорт', 'EXPENSE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE user_id IS NULL AND name = 'Транспорт'
);

INSERT INTO categories (user_id, name, type)
SELECT NULL, 'Подписки', 'EXPENSE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE user_id IS NULL AND name = 'Подписки'
);

INSERT INTO categories (user_id, name, type)
SELECT NULL, 'Инвестиции', 'EXPENSE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE user_id IS NULL AND name = 'Инвестиции'
);

INSERT INTO categories (user_id, name, type)
SELECT NULL, 'Прочее', 'EXPENSE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories WHERE user_id IS NULL AND name = 'Прочее'
);

