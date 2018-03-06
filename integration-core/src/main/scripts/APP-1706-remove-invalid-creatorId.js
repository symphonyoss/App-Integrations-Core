function removeInvalidFieldValues() {
  db.getCollection('integrationconfiginstance').update(
    { $or: [
      { "creatorId": /([^0-9]|^$)/g },
      { "creatorId": null },
      { "creatorId": {$exists: 0} }
    ]},
    { $set: {"creatorId": "0"} }
  )
}


function validate() {
  var invalidDocuments = db.getCollection('integrationconfiginstance').find(
    { $or: [
        { "creatorId": /([^0-9]|^$)/g },
        { "creatorId": null },
        { "creatorId": {$exists: 0} }
    ]}
  );
    
  if (invalidDocuments.count() > 0) {
    printjson({
        response: false,
        message: "Failed to updated invalid fields"
    });
  } else {
    printjson({
        response: true,
        message: "Successfully updated invalid fields"
    });
  }
}

removeInvalidFieldValues();
validate();
