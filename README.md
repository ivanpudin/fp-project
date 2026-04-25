# Getting Started

to download repo: `git clone https://github.com/ivanpudin/dsm-project.git`

# Making changes

the main project is located in the main branch.

you can pull the latest version of the project using: `git pull origin main` (ensure you are on the main branch)

we use branching and pull requests for introducing changes in order not to mess up the code

before making any changes ensure you are on the main branch with the latest code, and then switch to new branch

branch creates a local duplicate of the main branch which you can work on

this lists all branches and show which you are currently at: `git branch`

create new branch: `git branch branch_name`

switch branches by: `git checkout branch_name`

once you are done:

`git add .`

`git commit -m "message"`

`git push origin branch_name`

after that you can chekout back to the main branch and delete your branch locally: `git branch -d branch_name`

once you push the changes, create pull request to merge your branch into main
