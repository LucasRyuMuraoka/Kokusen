-- ==========================
-- Clans
-- ==========================
INSERT INTO CLAN (ID, NAME, DESCRIPTION) VALUES (1, 'Zenin', 'Clã tradicional do mundo jujutsu');
INSERT INTO CLAN (ID, NAME, DESCRIPTION) VALUES (2, 'Gojo', 'Clã com linhagem poderosa');
INSERT INTO CLAN (ID, NAME, DESCRIPTION) VALUES (3, 'Kamo', 'Clã antigo e especialista em manipulação de sangue');
INSERT INTO CLAN (ID, NAME, DESCRIPTION) VALUES (4, 'Sem Clã', 'Personagens que não pertencem a nenhum clã');
INSERT INTO CLAN (ID, NAME, DESCRIPTION) VALUES (5, 'Escola Jujutsu', 'Personagens afiliados à escola Jujutsu');

-- ==========================
-- Techniques
-- ==========================
INSERT INTO TECHNIQUE (ID, NAME, DESCRIPTION) VALUES (1, 'Limitless', 'Manipulação do espaço ao infinito');
INSERT INTO TECHNIQUE (ID, NAME, DESCRIPTION) VALUES (2, 'Ten Shadows', 'Controle de dez shikigamis através de sombras');
INSERT INTO TECHNIQUE (ID, NAME, DESCRIPTION) VALUES (3, 'Blood Manipulation', 'Controle avançado do sangue do usuário');
INSERT INTO TECHNIQUE (ID, NAME, DESCRIPTION) VALUES (4, 'Heavenly Restriction', 'Condição que concede força física imensa em troca de energia amaldiçoada');

-- ==========================
-- Characters (sorcerer table)
-- ==========================
INSERT INTO SORCERER (ID, NAME, RANK, CLAN_ID) VALUES (1, 'Satoru Gojo', 'SPECIAL_GRADE', 2);
INSERT INTO SORCERER (ID, NAME, RANK, CLAN_ID) VALUES (2, 'Maki Zenin', 'GRADE_4', 1);
INSERT INTO SORCERER (ID, NAME, RANK, CLAN_ID) VALUES (3, 'Megumi Fushiguro', 'GRADE_2', NULL);
INSERT INTO SORCERER (ID, NAME, RANK, CLAN_ID) VALUES (4, 'Noritoshi Kamo', 'GRADE_1', 3);
INSERT INTO SORCERER (ID, NAME, RANK, CLAN_ID) VALUES (5, 'Toji Fushiguro', 'GRADE_1', 1); -- sem técnica, sem expansão
INSERT INTO SORCERER (ID, NAME, RANK) VALUES (6, 'Random Civilian', 'NON_SORCERER'); -- exemplo de não feiticeiro

-- ==========================
-- Relation character_technique (many-to-many join table)
-- ==========================
INSERT INTO CHARACTER_TECHNIQUE (CHARACTER_ID, TECHNIQUE_ID) VALUES (1, 1); -- Gojo → Limitless
INSERT INTO CHARACTER_TECHNIQUE (CHARACTER_ID, TECHNIQUE_ID) VALUES (2, 4); -- Maki → Heavenly Restriction
INSERT INTO CHARACTER_TECHNIQUE (CHARACTER_ID, TECHNIQUE_ID) VALUES (3, 2); -- Megumi → Ten Shadows
INSERT INTO CHARACTER_TECHNIQUE (CHARACTER_ID, TECHNIQUE_ID) VALUES (4, 3); -- Noritoshi → Blood Manipulation
INSERT INTO CHARACTER_TECHNIQUE (CHARACTER_ID, TECHNIQUE_ID) VALUES (5, 4);

-- ==========================
-- Domain Expansions (one-to-one, owner references character id)
-- ==========================
INSERT INTO DOMAIN_EXPANSION (ID, NAME, EFFECT, OWNER_ID)
VALUES (1, 'Unlimited Void', 'Expansão de domínio de Gojo', 1);

INSERT INTO DOMAIN_EXPANSION (ID, NAME, EFFECT, OWNER_ID)
VALUES (2, 'Chimera Shadow Garden', 'Domínio de Megumi que amplia o poder das sombras', 3);

INSERT INTO DOMAIN_EXPANSION (ID, NAME, EFFECT, OWNER_ID)
VALUES (3, 'Malevolent Shrine', 'Domínio de Sukuna que aplica o acerto garantido de cortes', NULL);

ALTER SEQUENCE sorcerer_SEQ RESTART WITH 7;
ALTER SEQUENCE clan_SEQ RESTART WITH 6;
ALTER SEQUENCE technique_SEQ RESTART WITH 5;
ALTER SEQUENCE domain_expansion_SEQ RESTART WITH 4;