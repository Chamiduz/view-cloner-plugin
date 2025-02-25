# View-cloner
This plugin allows you to copy and change views and jobs assigned to them in bulk.

# Description
The plugin adds a build step that allows specifying a view (Nested and Sectioned views are supported) and a replacement pattern to use when copying and reconfiguring the jobs.

## Password Encryption Feature
With the latest update, passwords are now securely encrypted before being stored. This ensures that sensitive information is protected and complies with security best practices.

Expected results after the build step successfully executes are:
* **Original view and jobs assigned to it are left untouched**
* A new view is created next to the original view, preserving the structure of the view, and assigned jobs are changed to the newly created ones.
* Jobs assigned to the original view get copied after their names and configurations undergo a replacement.
* If, when creating a new job, the job name is already taken, the creation of that particular job is skipped.
