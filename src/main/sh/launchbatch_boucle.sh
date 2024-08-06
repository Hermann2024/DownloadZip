#!/bin/sh
# lancement de batch par URL
# compatible wget 1.13

if [ $# -ne 1 ]
then
	echo "Usage: launchBatch.sh [BATCH_NAME]"
	exit 1
fi

#################
#Parametres a adapter
UR_URL="http://serveur-batch:8080/webur"
LOGIN="admin" 
PASS="cylande"
#Repertoire de traitement
LAUNCH_DIR="/tmp"
#Temporisation pour check status
REFRESH_RATE=10
#################

#Fichier de retour du lancement du batch pour exploitation des resultats
RESULT_FILE="result.xml"
COOKIE_FILE="cookies.txt"
BATCH_NAME=$1
ECHO=/bin/echo

#Repertoire de travail
cd $LAUNCH_DIR

#Repertoire de traitement
TRT_DIR=$LAUNCH_DIR/$$
mkdir $TRT_DIR
cd $TRT_DIR

#appel initial et recuperation du cookie de session
wget -o out --save-cookies=$TRT_DIR/$COOKIE_FILE --keep-session-cookies "$UR_URL/tools"
#authentification
wget -o out --load-cookies=$TRT_DIR/$COOKIE_FILE --save-cookies=$TRT_DIR/$COOKIE_FILE --keep-session-cookies --post-data="j_username=$LOGIN&j_password=$PASS" "$UR_URL/j_security_check"
#lancement du bach
wget -a out -O $RESULT_FILE --load-cookies=$TRT_DIR/$COOKIE_FILE --keep-session-cookies "$UR_URL/batchEngine?action=execute&batchName=$BATCH_NAME&domain=default"
#extraction du batchId
BATCH_ID=`grep "<id>" $RESULT_FILE | tr -d '\r' | sed -e "s/<id>\([0-9]*\)<\/id>/\1/"`
#Sauvegarde du result file de lancement
mv $RESULT_FILE $RESULT_FILE.bak

#Traitement des résultats
if [ "test$BATCH_ID" != "test" ]
then
  #lancement OK suite a attribution du batchId
  #sleep 1
  $ECHO "Batch $BATCH_ID lancé."
  touch ${TRT_DIR}/${BATCH_ID}_en_cours
  
  RETRY=0
  CCC=0
  
  while true
  do
      let RETRY=RETRY+1
      wget -a out -O $RESULT_FILE --load-cookies=$TRT_DIR/$COOKIE_FILE --keep-session-cookies "$UR_URL/batchEngine?action=status&xsl=true&batchName=$BATCH_NAME&batchId=$BATCH_ID"
      CRWGET=$?
      #echo ${CRWGET}
  
      if [ -s $TRT_DIR/$RESULT_FILE ]
      then
          echo " Analyse de la balise <state> dans $TRT_DIR/$RESULT_FILE"
      else
          case ${CRWGET} in
  
              4) echo "  PROBLEME de resolution dns pour l'url : $UR_URL"
                 echo " Retrying. (${RETRY})"	
                 echo " "
                 if [ ${CCC} -eq 0 ]
                 then
                     CCC=360
                 fi
  
                 let CCC=CCC-1
                 sleep ${REFRESH_RATE}
                 continue
                 ;;

              *) echo "  PROBLEME, Le fichier $TRT_DIR/$RESULT_FILE est vide ou manquant"
                 return 11
                 ;;
          esac
      fi

      BATCH_STATE=$(grep "<state>" $TRT_DIR/$RESULT_FILE | head -1 | awk -F "<state>" '{print $2}' | awk -F "</state>" '{print$1}')
      PROGRESS=$(grep "<progress>" $TRT_DIR/$RESULT_FILE | head -1 | awk -F "<progress>" '{print $2}' | awk -F "</progress>" '{print$1}')
      if [ -z "${PROGRESS}" ]
      then
          PROGRESS="0"
      fi

      echo "   state = ${BATCH_STATE}"
      echo " "

      if [ "${BATCH_STATE}" = "true" ]
      then
          #Batch termine
          break
      else
          #Batch en cours
          echo "   progress = ${PROGRESS}"
      fi

      # On recherche une balise <problem>
      BATCH_PROBLEM=$(grep "<problem>" $TRT_DIR/$RESULT_FILE | head -1 | awk -F "<problem>" '{print$2}' | awk -F "</problem>" '{print$1}')
      if [ -n "${BATCH_PROBLEM}" ]
  	  then
          #Batch KO
          echo " [KO]  Execution du service ${batchName} (batchID : $BATCH_ID) - [KO]"
          echo " [PROBLEM]  ${BATCH_PROBLEM}"
          exit 11
      fi

      sleep ${REFRESH_RATE}
  done

  #Nettoyage du rep de travail
  cd $LAUNCH_DIR
  rm -fr $TRT_DIR
else
  #lancement KO 
  NOW=$(date +"%m%d%Y_%H%M%s")
  #archivage du repertoire temporaire
  tar cf error_$$_$NOW.tar $TRT_DIR/*
  cd $LAUNCH_DIR
  rm -fr $TRT_DIR
  exit 1	
fi
