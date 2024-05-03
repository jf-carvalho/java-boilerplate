-- inserting roles
INSERT INTO `roles` (`id`, `name`) VALUES (1, 'super'), (2, 'common user');

-- inserting permissions
INSERT INTO `permissions` (`id`, `name`) VALUES
(1, 'create users'),
(2, 'retrieve users'),
(3, 'update users'),
(4, 'delete users'),
(5, 'retrieve user roles'),
(6, 'update user roles');

-- associating permissions with roles
INSERT INTO `roles_permissions` (`role_id`, `permission_id`) VALUES
(2, 1),
(2, 2),
(2, 3),
(2, 4),
(2, 5),
(2, 6);

-- associating users with roles
INSERT INTO `users_roles` (`user_id`, `role_id`) VALUES (1, 1);