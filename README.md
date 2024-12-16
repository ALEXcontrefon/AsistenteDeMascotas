## BORRADOR DE PROYECTO - ASISTENTE DE CUIDADO DE MASCOTAS 



### Avances Realizados

- **Creación de Activities**: Se han creado tres actividades principales:
  - `LoginActivity.kt`: Para iniciar sesión en la aplicación.
  - `MainActivity.kt`: Pantalla principal donde se gestionan las mascotas y los recordatorios.
  - `RegisterActivity.kt`: Para registrar una nueva cuenta de usuario.
  
- **Creación de Layouts**: Se han diseñado los siguientes archivos XML para las pantallas y diálogos de la aplicación:
  - `activity_login.xml`: Layout para la pantalla de inicio de sesión.
  - `activity_main.xml`: Layout para la pantalla principal donde se gestionan las mascotas y recordatorios.
  - `activity_register.xml`: Layout para la pantalla de registro de usuarios.
  - `dialog_add_pet.xml`: Layout para agregar una nueva mascota.
  - `dialog_add_reminder.xml`: Layout para agregar un nuevo recordatorio.

- **Colores**: Se ha trabajado en la personalización de la aplicación mediante la adición de diferentes colores en la carpeta `values`.

- **Firebase y Firestore**:
  - Se ha implementado correctamente el inicio de sesión mediante Firebase, donde el correo electrónico del usuario se guarda en la base de datos.
  - En Firestore, se ha logrado que las mascotas se agreguen correctamente, aunque actualmente hay un error que impide que las mascotas carguen en la lista de la pantalla principal (`MainActivity`).

---
### Avances Penúltimo Módulo

En esta etapa del desarrollo del proyecto, se han implementado las siguientes mejoras y ajustes:

- Se añadieron todos los datos de las mascotas para mostrarlos en la pantalla correspondiente.
- Se desactivaron las sugerencias al escribir en los campos de texto para mejorar la experiencia del usuario.
- Se corrigió el proceso de edición de la especie de las mascotas.
- Se integró y visualizó correctamente el logo de la aplicación.
- Se añadió una barra de carga para indicar procesos en segundo plano.
- Se incluyó la funcionalidad para seleccionar el sexo del animal al registrarlo.
- Se ajustaron los textos de los elementos Spinner para mayor claridad.
- Se añadió una pantalla de carga inicial que saluda al usuario al abrir la aplicación.
- Al crear un nuevo usuario, se añadió la capacidad de guardar información básica en Firestore (nombre, apellido, número de teléfono y fecha de nacimiento), aunque estos datos no se muestran en pantalla.

---

### Avances Último Módulo
En esta etapa final del desarrollo del proyecto, se han implementado las siguientes mejoras y funcionalidades adicionales:

- Se añadió un botón "+" en las listas para mostrar información adicional tanto de las mascotas como de los recordatorios, mostrando inicialmente solo el nombre de cada elemento.
- Se implementó una validación que impide guardar recordatorios si la fecha y hora seleccionadas son anteriores a la actual.
- Se agregó la posibilidad de modificar los recordatorios después de ser notificados, permitiendo su edición sin necesidad de crear uno nuevo.
- Se añadió una funcionalidad para reutilizar recordatorios previamente configurados, facilitando la gestión de tareas recurrentes.
- Se habilitó la opción para seleccionar una mascota específica al crear o asociar recordatorios, mejorando la personalización de las actividades.
- Se implementó una notificación automática basada en la fecha y hora seleccionadas al momento de crear un recordatorio, asegurando que las alertas lleguen en el momento adecuado.
- Se mejoró el diseño de las listas, otorgándoles un estilo más claro y ordenado que facilita la visualización de la información.
- Se añadió la capacidad de modificar la información del recordatorio y de las actividades asociadas después de haber sido creadas, optimizando la experiencia del usuario.







### 1. Descripción del Proyecto 

La aplicación“Asistente de Cuidado de Mascotas” tiene el objetivo de ser diseñada para ayudar a los dueños de mascotas a mantener un registro organizado de las necesidades de cuidado de sus animales, esto gracias a que esta aplicación permitirá a los dueños programar recordatorios para citas veterinarias, horarios de alimentación y fechas de vacunación, como tal, su objetivo principal es centralizar la información y las tareas de cuidado de las mascotas, brindando a los usuarios una solución digital que simplifica el seguimiento y la administración de estos compromisos intentando dejar de lado los métodos tradicionales con papel o cartón. 

### 2. Exposición del Problema 

A día de hoy, en mi hogar cuento con la compañía de tres mascotas, dos de ellas son perros, y una es una gatita, y al igual que a mí, muchos dueños de mascotas enfrentan dificultades para recordar fechas importantes relacionadas con el cuidado de sus animales, como puede ser las citas veterinarias, las vacunas o los horarios de alimentación, por lo que una falta de un sistema de recordatorios centralizado y fácil de usar aumenta aún más esa probabilidad de olvidos que puedan llegar a afectar la salud y el bienestar de nuestras mascotas, por lo que, con esta aplicación, lo que buscamos es resolver este problema proporcionando un sistema que notifique al usuario a tiempo, asegurando que el cuidado de la mascota sea adecuado y constante. 

### 3. Plataforma 

En un inicia, la aplicación será desarrollada para el sistema operativo Android, esto con el objetivo de asegurar la compatibilidad con una amplia gama de dispositivos, por lo que Android es ideal por su alta penetración en el mercado y porque ofrece herramientas y recursos de desarrollo que facilitan la implementación de funcionalidades, como notificaciones y almacenamiento local. 

### 4. Interfaz de Usuario e Interfaz de Administrador 

##### Debido a que se trata de un proyecto, es importante entender que este apartado podría llegar a estar sujeto a cambios

- Interfaz de Usuario: lo ideal es que sea simple y accesible, permitiendo al usuario agregar mascotas, registrar citas, establecer horarios de alimentación y vacunar a sus mascotas, en donde cada tarea se puede programar con alertas y notificaciones, esto mediante el uso de una navegación intuitiva, con opciones claras y etiquetas identificables para cada función. 

- Interfaz de Administrador: Como aplicación para uso personal, el usuario actúa también como el administrador, gestionando toda la información relacionada con sus mascotas, esto se podrá realizar a través de esta interfaz en donde se podrá añadir, editar y eliminar información de forma segura y directa. 

### 5. Funcionalidad 

- Como inicio la aplicación buscara incluir las siguientes funcionalidades clave: 

- Registro de Mascotas: Permite agregar y almacenar los datos de cada mascota, incluyendo el nombre, el tipo (perro, gato, etc.), la raza, le edad y una o varias fotos. 

- Citas Veterinarias: El usuario podrá programar citas veterinarias, con notificaciones previas que vayan recordando al usuario del compromiso que tiene. 

- Vacunación: Permite programar las fechas de vacunación con alertas recordatorias. 

- Horarios de Alimentación: Los usuarios pueden configurar recordatorios para alimentar a sus mascotas en horarios específicos, o en caso de ser necesario un recordatorio de compra de alimentos. 

- Notificaciones: Recordatorios automáticos de cada tarea pendiente. 

### 6. Diseño (Wireframes o Esquemas de Página) 

##### Al igual que la interfaz del usuario y la interfaz de administrador, debido a que se trata de un proyecto, este apartado podría llegar a estar sujeto a cambios futuros

- Pantalla de Inicio: Presentará un listado con las mascotas registradas y un botón para agregar nuevas. 

- Pantalla de Detalles de Mascota: Al seleccionar una mascota, mostrará detalles básicos y las opciones de citas, vacunación y alimentación. 

- Pantalla de Configuración de Recordatorios: Una sección para añadir y configurar los recordatorios. 

- Pantalla de Notificaciones: Donde el usuario podrá ver todas las notificaciones recientes y próximas. 
