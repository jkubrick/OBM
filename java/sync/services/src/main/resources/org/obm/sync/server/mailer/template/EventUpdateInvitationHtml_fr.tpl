<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Invitation à un évènement : mise à jour
        </th>
    </tr>
    <tr>
        <td colspan="2">Le rendez-vous <strong><?php echo $title; ?></strong>, initialement prévu du ${old.start?string.medium_short} au ${old.end?string.medium_short}, (lieu : ${old.location}),
a été modifié :</td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%;padding-right:1em;">Sujet</td><td style="font-weight:bold;">${new.subject}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Du</td><td style="font-weight:bold;">${new.start?string.medium_short}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Au</td><td style="font-weight:bold;">${new.end?string.medium_short}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Lieu</td><td style="font-weight:bold;">${new.location}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Organisateur</td><td style="font-weight:bold;">${new.organizer}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Créé par</td><td style="font-weight:bold;">${new.creator}</td>
    </tr>
    <tr valign="top">
        <td style="text-align:right;padding-right:1em;">Participant(s)</td><td style="font-weight:bold;">${new.attendees}</td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=ACCEPTED">Accepter</a>
          <a href="${host}calendar/calendar_index.php?action=update_decision&calendar_id=${calendarId}&entity_kind=user&rd_decision_event=DECLINED">Refuser</a>
          <a href="${host}calendar/calendar_index.php?action=detailconsult&calendar_id=${calendarId}">Consulter l'agenda</a>
        </td>
    </tr>
</table>
