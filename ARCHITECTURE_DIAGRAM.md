# Component Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      DashboardController                        │
│  (320 lines - Coordinator Pattern)                             │
├─────────────────────────────────────────────────────────────────┤
│  - stage: Stage                                                 │
│  - admin: Admin                                                 │
│  - eventService: EventService                                   │
│  - navigationMenu: NavigationMenu                               │
│  - contentArea: StackPane                                       │
├─────────────────────────────────────────────────────────────────┤
│  + getScene(): Scene                                            │
│  + setupNavigationHandlers(): void                              │
│  + showHomePage(): void                                         │
│  + showEventsPage(): void                                       │
│  + showSeatsPage(): void                                        │
│  + showBookingsPage(): void                                     │
│  + showReportsPage(): void                                      │
└─────────────────────────────────────────────────────────────────┘
                    │
                    │ uses
                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        Component Package                             │
│                   (org.openjfx.component)                            │
└──────────────────────────────────────────────────────────────────────┘
        │
        ├─────────────────────────────────────────────────────┐
        │                                                     │
        ▼                                                     ▼
┌─────────────────────┐                           ┌─────────────────────┐
│  NavigationMenu     │                           │    StatCard         │
│  extends VBox       │                           │    extends VBox     │
├─────────────────────┤                           ├─────────────────────┤
│ - admin: Admin      │                           │ - valueLabel: Label │
│ - homeButton        │                           ├─────────────────────┤
│ - eventsButton      │                           │ + setValue(String)  │
│ - seatsButton       │                           └─────────────────────┘
│ - bookingsButton    │
│ - reportsButton     │
├─────────────────────┤
│ + highlightButton() │
│ + getHomeButton()   │
│ + getEventsButton() │
└─────────────────────┘
        │
        ├─────────────────────────────────────────────────────┐
        │                                                     │
        ▼                                                     ▼
┌─────────────────────┐                           ┌─────────────────────┐
│  EventTableView     │                           │   EventFilterBar    │
│  extends TableView  │                           │   extends HBox      │
├─────────────────────┤                           ├─────────────────────┤
│ + loadEvents(List)  │                           │ - searchField       │
└─────────────────────┘                           │ - typeFilter        │
                                                  │ - statusFilter      │
        │                                         │ - refreshBtn        │
        │                                         ├─────────────────────┤
        │                                         │ + reset(): void     │
        ▼                                         └─────────────────────┘
┌─────────────────────┐                           
│  EventFormDialog    │◄──────────────┐
├─────────────────────┤               │
│ - dialog: Stage     │               │
│ - existingEvent     │               │ shows
│ - onSuccess: Runnable               │
├─────────────────────┤               │
│ + show(): void      │               │
│ - validateForm()    │               │
│ - handleSave()      │               │
└─────────────────────┘               │
                                      │
┌─────────────────────┐               │
│DeleteConfirmation   │───────────────┘
│Dialog               │
├─────────────────────┤
│ - dialog: Stage     │
│ - event: Event      │
│ - onSuccess: Runnable
├─────────────────────┤
│ + show(): void      │
│ - handleDelete()    │
└─────────────────────┘


┌──────────────────────────────────────────────────────────────────┐
│                     Data Flow Example                            │
└──────────────────────────────────────────────────────────────────┘

User clicks "Events" button
    ↓
NavigationMenu.getEventsButton().onAction()
    ↓
DashboardController.showEventsPage()
    ↓
┌─────────────────────────────────────┐
│ 1. Create EventFilterBar            │
│ 2. Create EventTableView            │
│ 3. Load data via EventService       │
│ 4. Setup filter handlers            │
│ 5. Wrap in ScrollPane               │
│ 6. Display in contentArea           │
└─────────────────────────────────────┘
    ↓
User clicks "Create Event"
    ↓
DashboardController.showCreateEventDialog()
    ↓
new EventFormDialog(stage, null, this::showEventsPage).show()
    ↓
User fills form and clicks "Create"
    ↓
EventFormDialog.handleSave()
    ↓
EventService.createEvent(newEvent)
    ↓
EventFormDialog closes and calls onSuccess
    ↓
DashboardController.showEventsPage() (refresh)


┌──────────────────────────────────────────────────────────────────┐
│                     Component Responsibilities                   │
└──────────────────────────────────────────────────────────────────┘

DashboardController:
  ✓ Coordinate navigation
  ✓ Manage page transitions
  ✓ Handle service calls
  ✗ UI rendering details (delegated to components)

NavigationMenu:
  ✓ Display menu structure
  ✓ Highlight active button
  ✗ Know about page content (uses callbacks)

EventTableView:
  ✓ Display event data
  ✓ Handle row actions
  ✗ Load data (accepts List<Event>)

EventFilterBar:
  ✓ Display filter controls
  ✓ Expose controls via getters
  ✗ Implement filter logic (controller does this)

EventFormDialog:
  ✓ Display form
  ✓ Validate input
  ✓ Save to database
  ✓ Call success callback
  ✗ Know what happens after success

DeleteConfirmationDialog:
  ✓ Show confirmation
  ✓ Delete from database
  ✓ Call success callback
  ✗ Know what happens after deletion


┌──────────────────────────────────────────────────────────────────┐
│                     Old vs New Architecture                      │
└──────────────────────────────────────────────────────────────────┘

OLD (Monolithic):
┌────────────────────────────┐
│  DashboardController       │
│  (700 lines)               │
│                            │
│  Everything mixed:         │
│  - Navigation              │
│  - Page rendering          │
│  - Table creation          │
│  - Form dialogs            │
│  - Validation              │
│  - Service calls           │
│  - Event handlers          │
└────────────────────────────┘


NEW (Component-Based):
┌────────────────────────────┐
│  DashboardController       │
│  (320 lines)               │
│  Coordinates components    │
└────────────────────────────┘
           │
           │ delegates to
           ▼
┌────────────────────────────┐
│     6 Focused Components   │
│                            │
│  NavigationMenu (150)      │
│  EventTableView (140)      │
│  EventFormDialog (200)     │
│  DeleteConfDialog (100)    │
│  EventFilterBar (60)       │
│  StatCard (40)             │
└────────────────────────────┘
```
