# crud
crud aplicacion web para el taller jmoordb

Clonamos el proyecto
Creamos una carpeta
mkdir template
cd template
clonamos el proyecto
git clone https://github.com/avbravo/crud.git
Entramos al directorio crud
cd crud

Ejecutar
mvn archetype:create-from-project

Genera el arquetipo
Observe la información generada
Setting default groupId: com.avbravo
[INFO] Setting default artifactId: crud
[INFO] Setting default version: 0.1
[INFO] Setting default package: com.avbravo.crud
ahora entramos a la carpeta
 cd target/generated-sources/archetype/
Ejecutar
mvn install

Nos harán unas preguntas:
1: local -> com.avbravo:crud-archetype (crud-archetype) Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): :
Responder : 1

Define value for property 'groupId': 
Responder:com.avbravo

Define value for property 'artifactId': 
myweb

Define value for property 'version' 1.0-SNAPSHOT: 
Responder: 0.1
Define value for property 'package' com.avbravo: 
Responder: com.avbravo

Confirmar con Y

Genera el proyecto
