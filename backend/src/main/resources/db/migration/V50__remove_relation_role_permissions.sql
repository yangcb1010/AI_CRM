DELETE FROM manager_role_menu
WHERE menu_id IN (
    SELECT menu_id
    FROM manager_menu
    WHERE realm = 'relation'
       OR realm LIKE 'relation:%'
);

DELETE FROM manager_menu
WHERE realm = 'relation'
   OR realm LIKE 'relation:%';
