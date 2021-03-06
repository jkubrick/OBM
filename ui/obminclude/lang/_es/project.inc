<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Español                                                  //
//     - File     : project.inc                                              //
// 2006-02-24 Jaime E. Gomez H.   jgomez@skinatech.com                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_tasktype = "Categoría";
$l_task = "Tarea";
$l_soldtime = "Tiempo vendido";
$l_estimatedtime = "Tiempo Estimado";

$l_sold = "Vendido";
$l_estimated = "Estimado";
$l_allotted = "Alloted";
$l_projected = "Proyectado";
$l_used = "Usado";
$l_missing = "Perdido";

$l_alloestimated = "tiempo reservado proyectado";
$l_usedestimated = "tiempo a usar proyectado";
$l_progress = "Progreso del Proyecto";
$l_progeval = "Evaluacion de Avance/Retraso";

// Labels et textes
$l_parent = "Tarea Padre";
$l_soldtime = "Tiempo vendido";
$l_projtime = "Tiempo proyectado";
$l_total = "Total";
$l_resume = "Resumen";
$l_tasks = "Tareas";

// Titres
$l_members_info = "Miembros del Proyecto";
$l_progressinfos = "Evaluación de progreso: las siguientes datos son<br>(tiempo usado / tiempo perdido / tiempo total proyectoado)";
$l_memberstimeinfos = "Llene la duración proyectada del trabajo (en días) para cada persona asignada a una tarea";
$l_fill_advance_form = "Ingrese el tiempo de trabajo : planeado / restante";
$l_task_new = "Nueva tarea";
$l_task_update = "Actualice tarea";
$l_task_list = "Tareas definidas";
$l_progress_info = "Llene la duración perdida del trabajo (en dias) para cada persona para completar una tarea";
$l_select_project = "Seleccionar proyecto";

// Actions
$l_insert = "Crear proyecto";
$l_update = "Actualizar proyecto";
$l_delete = "Borrar proyecto";
$l_task_add = "Adicionar tarea";
$l_task_upd = "Actualice tarea";
$l_task_del = "Borrar tarea seleccionada";
$l_progress_update = "Actualizar proyecto en progreso";

// Messages
$l_no_display = "Llene los campos de búsqueda y presione < Buscar > para obtener los proyectos.";
$l_found = "proyecto(s) encontrados.";
$l_no_found = "No hay proyectos correspondientes a su búsqueda !";
$l_no_tasks = "Aún no hay tareas definidas.";
$l_no_members = "No hay información disponible acerca de los miembros del proyecto";
$l_no_allocation = "Aún no se han asignado tareas a los miembros del proyecto";
$l_link_timetask = "El proyecto es usado en el control de tiempos";
$l_link_timetask_no = "El proyecto NO es usado en el control de tiempos";
$l_cant_delete = "El proyecto NO puede ser borrado !";
$l_can_delete = "El proyecto puede ser borrado.";
$l_no_task_del = "No hay tareas para remover";
$l_ptask_same_task = "Una tarea padre no puede ser igual a la tarea !";
$l_ptask_and_parent = "Una tare padre o puede tener un padre !";
$l_ptask_has_parent = "La tarea asignada como padre ya tiene un padre !";
$l_ptask_has_user = "La tarea asignada como padre esta vinculad con un usuario !";

$l_member_delete_error = "La eliminación del miembro ha fallado ! Debe tener asignada alguna tarea o ha gastado algun tiempo en el proyecto"; 

$l_del_member_sel = "eliminar estos miembros";
$l_member_added = "miembros(s) adicionados";
$l_member_removed = "miembros(s) removidos";
$l_no_member_add = "No se adicionó el miembro";
$l_no_member_del = "No se borro el miembro";
$l_add_member = "Adicione miembros al proyecto";
$l_board_no_data = "Tablero no puede ser mostrado si la definición de tareas o miembros no existen !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Debe ingresar el nombre antes de enviar.";
$l_fill_tt = "Debe ingresar la categoría antes de enviar.";
$l_fill_soldtime = "Debe ingresar el tiempo vendido antes de enviar.";
$l_fill_estimated = "Debe ingresar el tiempo estimado antes de enviar.";
$l_fill_tasklabel = "Debe ingresar el nombre de esta tarea.";
$l_fill_soldnumber = "El número del tiempo vendido no es válido.";
$l_fill_projected = "Cada miembro debe ser asignado a una tarea";
$l_fill_projnumber = "El número de tiempo del proyecto no es válido.";
$l_fill_missing = "El tiempo perdido debe ser llenado para cada miembro";
$l_fill_missnumber = "El número de tiempo no es válido.";
$l_delete_confirm = "Confirme la eliminación del proyecto ?";

// For the display preferences modification screen 
$l_project_display = "Opciones de despliegue de listas de proyectos";
