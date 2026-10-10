# Definition of Done (DoD)

## 1. Overview

The Definition of Done (DoD) is a shared understanding of what it means for work to be complete on this project. All user stories, technical tasks, and milestone deliverables must satisfy the DoD before being considered finished.

The DoD applies at multiple levels:
- **User Story Level**: Each US-X must meet its acceptance criteria
- **Milestone Level**: Each of the 15 assignment milestones must have all deliverables complete
- **Release Level**: The full MVP must satisfy all success criteria before version tagging
- **Pipeline Level**: Jenkins pipeline must complete all stages with quality gates passing

## 2. DoD Checklist (Per User Story)

Every user story (US-1 through US-15) must satisfy ALL of the following before marking done:

### Functional Completeness
- [ ] All acceptance criteria from the user story document are met
- [ ] Feature works as described in the user story flow
- [ ] No critical bugs or broken workflows
- [ ] Role-specific behavior correct (Citizen/Technician/Admin)

### Code Quality
- [ ] Code follows Java conventions and Spring Best Practices
- [ ] No TODO comments left in production code (or documented as known limitations)
- [ ] Method and variable names are descriptive and in English
- [ ] Code compiles without errors via `./mvnw clean compile`
- [ ] Code passes any static analysis (checkstyle, if configured)

### Testing
- [ ] Unit tests written and passing (JUnit 5 + Mockito where applicable)
- [ ] Integration tests passing (Spring Boot Test with appropriate context)
- [ ] Selenium test for this user journey passes (if applicable)
- [ ] Negative test cases covered (invalid inputs, unauthorized access, etc.)
- [ ] Test assertions are meaningful (not just "true == true")

### Documentation
- [ ] Relevant documentation sections updated (docs/ directory)
- [ ] Mermaid diagrams updated if UI/architecture changed
- [ ] Code comments added for complex business logic
- [ ] README.md reflects current setup instructions

### Security
- [ ] Passwords stored as BCrypt hashes (not plain text)
- [ ] Role-based access enforced server-side (not just frontend hiding)
- [ ] Input validation on all form fields (server-side)
- [ ] CSRF protection enabled for all form submissions
- [ ] Unauthorized access attempts blocked (tested via Selenium)

### Did Not Impact Existing Functionality
- [ ] No existing broken functionality (regression testing)
- [ ] Related user stories still pass
- [ ] Dashboard metrics still accurate

## 3. Milestone-Level DoD

Each of the 15 assignment milestones must have these deliverables complete:

| # | Milestone | DoD Requirements |
|---|-----------|------------------|
| **1** | Problem Definition, Stakeholders, Objectives, Success Criteria | - `docs/problem-definition.md` exists and is populated<br>- `docs/stakeholders-and-objectives.md` exists and is populated<br>- Measurable success criteria defined and documented<br>- Constraints and assumptions documented |
| **2** | Agile Backlog, User Stories, Acceptance Criteria, 15-Task Plan, DoD, DevOps Lifecycle Diagram | - `docs/product-backlog.md` exists with all user stories<br>- `docs/mvp-scope.md` exists with MVP scope<br>- 15-task Kanban/Scrum plan documented<br>- DoD checklist documented (this file)<br>- DevOps lifecycle diagram (Mermaid) in docs/architecture.md or separate file |
| **3** | SRS Summary, Use-Case Diagram, Architecture, Database Model, API List, Local Setup | - `docs/srs.md` exists and populated<br>- `docs/use-cases.md` exists and populated<br>- `docs/architecture.md` exists with Mermaid diagrams<br>- `docs/database-design.md` exists with ER model<br>- `docs/api-reference.md` exists with endpoint list<br>- Local setup guide (README or docs/local-setup.md) |
| **4** | GitHub Repository Init, README, .gitignore, Issue Templates, Branch Rules | - Git repository initialized (`git init` or existing)<br>- `README.md` created and populated<br>- `.gitignore` excludes target/, .env, .m2/, credentials<br>- GitHub issue templates created (bug report, feature request)<br>- Branch rules documented (main/develop/feature/bugfix)<br>- Repository hosted on GitHub (or local with remote publishing docs) |
| **5** | First Feature Branch, Meaningful Commits, PR, Review, Merge | - Feature branch created (`feature/xxxx`)<br>- At least 3 meaningful conventional commit messages<br>- Pull Request created with description and checklist<br>- Code review completed (peers or self-review documented)<br>- PR merged to `develop` branch<br>- Git history shows branch flow and merge |
| **6** | Remaining MVP Features, Conflict Resolution, Release Tag, Backlog Update | - All MVP features implemented (see mvp-scope.md)<br>- Merge conflict between two branches occurred and was resolved<br>- Conflict resolution documented (what happened, how resolved)<br>- Release tag `v1.0.0` created and annotated on `main`<br>- Backlog updated to reflect completed items<br>- `main` branch stable (deployable) |
| **7** | Jenkins Installation/Configuration, CI Build Job, Triggers, Logs, Artifact | - Jenkins server accessible (or local pipeline configured)<br>- Jenkins job created for this project<br>- GitHub webhook configured (or polling documented)<br>- Build logs captured and readable<br>- JAR artifact archived in Jenkins<br>- Successful build run with all stages passing |
| **8** | Jenkinsfile Pipeline and Application Deployment with Parameterized Config | - `Jenkinsfile` at repository root<br>- All 12 pipeline stages present and functional<br>- Pipeline triggered on Git commit (webhook or polling)<br>- Parameterized configuration (Docker image, tag, registry)<br>- Application deployed via pipeline (docker compose up)<br>- Deployment verified working after pipeline completes |
| **9** | Selenium Test Design and Local Execution (including failure screenshots) | - 5 Selenium tests designed: login, submit complaint, search, status update, unauthorized restriction<br>- Tests run locally via `./mvnw test`<br>- Positive and negative test cases covered<br>- Useful failure screenshots captured when tests fail<br>- Test reports generated and readable |
| **10** | Selenium Integration into Jenkins, Quality Gates, Defect Correction, Successful Rerun | - Selenium tests integrated into Jenkins pipeline (stage 9)<br>- Failed tests block Docker build and deployment (quality gate)<br>- Deliberate defect introduced and corrected (screenshots/doc)<br>- Successful rerun of pipeline after fix (all stages pass)<br>- Test reports archived in Jenkins |
| **11** | Docker Image Build, Tagging, Execution, Inspection, Logs, Stop/Restart/Remove | - Dockerfile creates valid image<br>- Image built and tagged: `docker build -t <repo>:<tag>`<br>- Container executed: `docker run -p 8080:8080 <image>`<br>- Image inspected: `docker images`, `docker ps`<br>- Logs viewed: `docker logs <container>`<br>- Stop/Restart/Remove operations performed<br>- Docker Compose: `docker compose up -d` brings up full stack |
| **12** | Jenkins-to-Docker Automated Delivery, Registry Publishing, Commit-to-Container | - Commit to GitHub triggers Docker image build<br>- Image published to Docker Hub (credentials in Jenkins)<br>- Commit-to-container workflow documented<br>- Docker image versioned (tag builds)<br>- Pipeline: build → test → package → Docker → push → deploy<br>- Registry publishing verified (image visible on Docker Hub) |
| **13** | Ansible Configuration-Management Playbook and First Execution | - Inventory file created (`inventory.yml`) |