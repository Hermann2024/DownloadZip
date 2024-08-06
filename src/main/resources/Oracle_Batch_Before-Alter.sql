--
-- Mise � jour structure relationnelle sous Oracle DB
-- A jouer AVANT synchronisation avec dbSynchro
-- Permet de passer d'une structure en phase avec la derni�re version fig�e vers une structure en phase avec la version en cours de d�veloppement
--

-- A l'attention de la communaut� de d�veloppeurs :
-- > Pensez � pr�fixer tous les commentaires de '--'
-- > Veillez � ajouter vos mises � jour en ent�te du fichier (les plus anciennes sont en bas)
-- > Merci de pr�c�der votre mise � jour des informations suivantes : 
--   Date : jj/mm/aaaa
--   Autheur : nom pr�nom
--   Version : w.x.y-z -> Courante (remarque : w.x.y-z n'est pas �gal � 'Courante', il s'agit de la derni�re version fig�e : majeure (w.x), mineure (y), niveau de patch (z))
--   Comentaire

-- Date : 02/12/2009
-- Auteur : YAMOUNI Mohammed; GRUSZECKI JPh
-- Mise � jour de la table BATCH_RUN, TASK_RUN, TASK_AUDIT :
-- AJOUT DE LA COLONNE SITE_CODE EN TANT QUE CHAMP DE CLE PRIMAIRE
-- AVEC LA VALEUR LOCAL PAR DEFAUT

DECLARE
  Tmp INTEGER;
  LeNumeroUnique INTEGER;
BEGIN
  LeNumeroUnique := 47679; -- Num�ro unique
  BEGIN
    -- On recherche le num�ro si on trouve alors on ne fait rien
    SELECT 1 INTO Tmp
    FROM SQL_SCRIPT_LOG
    WHERE SCRIPT_ID = LeNumeroUnique;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      -- Si on n'a pas trouv� l'enregistrement alors il faut ex�cuter la moulinette
      
      -- MAJ de BATCH_RUN 
      -- ajout de la colonne SITE_CODE sans la contrainte NOT NULL
      execute immediate 'ALTER TABLE BATCH_RUN ADD SITE_CODE NVARCHAR2(16)';
      -- ajout du site 'LOCAL' dans la colonne SITE_CODE
      execute immediate 'UPDATE BATCH_RUN SET SITE_CODE = ''LOCAL''';
      -- ajout de la contrainte NOT NULL pour la colonne SITE_CODE
      execute immediate 'ALTER TABLE BATCH_RUN MODIFY SITE_CODE NOT NULL';
      -- supression de la cl� existante et de ses index
      execute immediate 'ALTER TABLE BATCH_RUN DROP PRIMARY KEY DROP INDEX';
      -- reconstitution de la cl�
      execute immediate 'ALTER TABLE BATCH_RUN ADD (CONSTRAINT BATCH_RUN_PK PRIMARY KEY (SITE_CODE, BATCH_CODE, ID))';

      -- MAJ de TASK_RUN
      -- ajout de la colonne SITE_CODE sans la contrainte NOT NULL
      execute immediate 'ALTER TABLE TASK_RUN ADD SITE_CODE NVARCHAR2(16)';
      -- ajout du site 'LOCAL' dans la colonne SITE_CODE
      execute immediate 'UPDATE TASK_RUN SET SITE_CODE = ''LOCAL''';
      -- ajout de la contrainte NOT NULL pour la colonne SITE_CODE
      execute immediate 'ALTER TABLE TASK_RUN MODIFY SITE_CODE NOT NULL';
      -- supression de la cl� existante et de ses index
      execute immediate 'ALTER TABLE TASK_RUN DROP PRIMARY KEY DROP INDEX';
      -- reconstitution de la cl�
      execute immediate 'ALTER TABLE TASK_RUN ADD (CONSTRAINT TASK_RUN_PK PRIMARY KEY (SITE_CODE, TASK_CODE, ID))';
      
      -- MAJ de TASK_AUDIT
      -- ajout de la colonne SITE_CODE sans la contrainte NOT NULL
      execute immediate 'ALTER TABLE TASK_AUDIT ADD SITE_CODE NVARCHAR2(16)';
      -- ajout du site 'LOCAL' dans la colonne SITE_CODE
      execute immediate 'UPDATE TASK_AUDIT SET SITE_CODE = ''LOCAL''';
      -- ajout de la contrainte NOT NULL pour la colonne SITE_CODE
      execute immediate 'ALTER TABLE TASK_AUDIT MODIFY SITE_CODE NOT NULL';
      -- supression de la cl� existante et de ses index
      execute immediate 'ALTER TABLE TASK_AUDIT DROP PRIMARY KEY DROP INDEX';
      -- reconstitution de la cl�
      execute immediate 'ALTER TABLE TASK_AUDIT ADD (CONSTRAINT TASK_AUDIT_PK PRIMARY KEY (SITE_CODE, TASK_CODE, TASK, ID))';
      
      -- Si tout se passe bien alors on ins�re dans la table SQL_SCRIPT_LOG le num�ro unique
      INSERT INTO SQL_SCRIPT_LOG (CREATION_TIME, SCRIPT_ID) VALUES (SYSDATE, LeNumeroUnique);
      COMMIT;
  END;    
END;
/
