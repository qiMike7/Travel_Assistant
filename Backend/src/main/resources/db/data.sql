-- ============================================================
-- Meta-Tourism 智趣AI旅行助手 种子数据（幂等，可重复执行）
-- 表结构由 JPA(ddl-auto=update) 自动创建，本脚本仅初始化基础数据
-- ============================================================

-- 商城商品（对应 Frontend/pages/guide/guide.vue）
INSERT INTO t_product (id, create_time, update_time, name, description, price, original_price, image, category, stock, sales, status) VALUES
 (1, NOW(), NOW(), '户外登山背包',   '轻便防水，适合登山徒步', 299.00, 399.00, '/static/shopping/bag.jpg',          '装备', 100, 0, 1),
 (2, NOW(), NOW(), '旅行收纳套装',   '多层收纳，整理神器',     89.00, 129.00, '/static/shopping/shouna.jpg',       '收纳', 100, 0, 1),
 (3, NOW(), NOW(), '文创笔记本',     '精美设计，记录美好时光',  19.00,  29.00, '/static/shopping/notebook.jpg',     '文创', 100, 0, 1),
 (4, NOW(), NOW(), '古镇手工茶具',   '传统工艺，品味文化',     599.00, 799.00, '/static/shopping/teacup.jpg',       '文创', 100, 0, 1),
 (5, NOW(), NOW(), '便携折叠水壶',   '折叠设计，携带方便',     159.00, 229.00, '/static/shopping/bottle.jpg',       '装备', 100, 0, 1),
 (6, NOW(), NOW(), '旅游充电宝',     '大容量，快充支持',        89.00, 139.00, '/static/shopping/chongdianbao.jpg', '装备', 100, 0, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), description = VALUES(description), price = VALUES(price),
  original_price = VALUES(original_price), image = VALUES(image), category = VALUES(category), status = VALUES(status);

-- 地图路线（对应 Frontend/pages/map-test/map-test.vue 的 polyline 轨迹）
INSERT INTO t_map_route (id, create_time, update_time, name, description, points, width, color, arrow_line) VALUES
 (1, NOW(), NOW(), '故宫周边漫步路线', '东城区经典徒步轨迹',
  '[{"latitude":39.907675,"longitude":116.397193},{"latitude":39.907627,"longitude":116.395395},{"latitude":39.907516,"longitude":116.391906},{"latitude":39.907388,"longitude":116.388825},{"latitude":39.907262,"longitude":116.384093},{"latitude":39.907239,"longitude":116.382813},{"latitude":39.907484,"longitude":116.382781},{"latitude":39.907074,"longitude":116.38281},{"latitude":39.907185,"longitude":116.387543},{"latitude":39.907424,"longitude":116.395395},{"latitude":39.907555,"longitude":116.39936},{"latitude":39.907626,"longitude":116.400936},{"latitude":39.907682,"longitude":116.402507},{"latitude":39.907784,"longitude":116.404191},{"latitude":39.907872,"longitude":116.406828},{"latitude":39.907947,"longitude":116.409509},{"latitude":39.907988,"longitude":116.411699},{"latitude":39.908082,"longitude":116.415304},{"latitude":39.9081,"longitude":116.415824},{"latitude":39.908172,"longitude":116.417368},{"latitude":39.908189,"longitude":116.41812}]',
  10, '#D9EEFB', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name), description = VALUES(description), points = VALUES(points),
  width = VALUES(width), color = VALUES(color), arrow_line = VALUES(arrow_line);
