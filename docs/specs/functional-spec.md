# Functional Specification — Rubik Solver

> **What** the product does, never **how**. Every requirement appears once, with a stable ID.
> Requirements: `FR-x.y` (functional), `NFR-x` (quality). Acceptance criteria: `AC-x.y` (Gherkin), one or more per
> requirement, named `AC-<requirement>.<n>` when a requirement has several (e.g. `AC-1.2.1`).

## 1. Vision

Anyone aged 6 and up can solve a Rubik's cube with the app today, and **without the app** tomorrow.
The app reads the user's real cube, shows how to solve it with an animated 3D guide, and teaches the techniques
step by step until the user no longer needs it.

## 2. Goals

| Goal | Measure of success |
|---|---|
| G1 Solve any real cube | A user with a scrambled cube reaches a solved cube by following the guide. |
| G2 Learn to solve alone | A user graduates a method: solves a random scramble with no hints (FR-5.6). |
| G3 For all ages | A 6-year-old can complete the first lesson without adult help. |
| G4 Trust | Works without internet; no personal data leaves the device. |

## 3. Personas

| Persona | Needs |
|---|---|
| **Kid learner** (6–12) | Short sentences, visuals over text, encouragement, no time pressure. |
| **Adult beginner** | Learn a first method at their own pace. |
| **Improver** | Move from the beginner method to faster methods (Ortega, CFOP, Roux). |
| **Just-solve-it user** | Get the cube solved quickly, with as few moves as possible. |

## 4. Ubiquitous language

| Term | Meaning |
|---|---|
| **Cube** | The physical puzzle. Supported **sizes**: 2x2 and 3x3. |
| **Face** | One of six sides: Up (U), Down (D), Front (F), Back (B), Left (L), Right (R). |
| **Sticker** | One colored square on a face (9 per face on 3x3, 4 on 2x2). |
| **Piece** | A physical block: center, edge or corner (2x2 has corners only). |
| **Color scheme** | The mapping of six colors to faces on a solved cube. |
| **Cube state** | The arrangement of all stickers at a moment in time. |
| **Valid state** | A cube state reachable from solved by turning layers. |
| **Move** | A single layer turn, written in standard notation (e.g. `R`, `U'`, `F2`). |
| **Notation** | The standard letters for moves. |
| **Algorithm** | A named sequence of moves with a purpose (e.g. "Sune"). |
| **Scramble** | A sequence of moves that mixes a solved cube. |
| **Method** | A complete strategy to solve a cube: Beginner, CFOP, Roux, Fast (3x3); Beginner, Ortega, Optimal (2x2). |
| **Stage** | A milestone of a method (e.g. "White cross", "F2L", "OLL"). |
| **Step** | One move of a solution, with its explanation. |
| **Solution** | The ordered stages and steps that take a cube state to solved. |
| **Guide** | The animated, step-by-step playback of a solution. |
| **Technique** | A learnable skill within a method (e.g. "Insert a corner", "2-look OLL"). |
| **Lesson** | A unit that teaches one technique. |
| **Hint level** | How much help the tutor gives, from full animation down to none. |
| **Mastery** | A technique the user performs reliably without hints. |
| **Graduation** | Solving a full scramble with a method, with no hints. |
| **Profile** | A local user, of kind **Kid** or **Adult**. |

## 5. Functional requirements

### FR-1 Cube input

- **FR-1.1** The user chooses the cube size (2x2 or 3x3) before entering a cube.
- **FR-1.2** The user scans the cube with the device camera, one face at a time. The app tells which face to show
  and how to hold the cube, and previews the detected colors live.
- **FR-1.3** Before confirming, the user reviews the full cube state and can correct any sticker.
- **FR-1.4** The user can enter the cube manually by painting stickers.
- **FR-1.5** The user can enter a scramble in notation; the app builds the resulting cube state.
- **FR-1.6** The default color scheme is the standard one (white opposite yellow, green front, red right).
  The user can define a custom scheme.

```gherkin
Scenario: AC-1.1 Size determines the input layout
  Given the user chose size "2x2"
  When the user starts entering a cube
  Then each face shows 4 stickers

Scenario: AC-1.2.1 Guided face-by-face scan
  Given the user chose size "3x3" and opened the scanner
  When the user captures all requested faces
  Then a complete cube state is proposed for review

Scenario: AC-1.2.2 Scanner guides the next face
  Given the user captured the Front face
  Then the app tells the user which face to show next and how to rotate the cube

Scenario: AC-1.3 Correct a misread sticker
  Given a proposed cube state where one sticker was read as orange but is red
  When the user changes that sticker to red
  Then the cube state shows that sticker as red

Scenario: AC-1.4 Manual entry
  Given an empty 3x3 cube
  When the user paints all 54 stickers and confirms
  Then the cube state is submitted for validation

Scenario: AC-1.5 Scramble entry
  Given a solved 3x3 cube
  When the user enters the scramble "R U R' U'"
  Then the cube state equals a solved cube after "R U R' U'"

Scenario: AC-1.6 Custom color scheme
  Given the user defined a scheme with blue in front
  When the user enters a cube
  Then the scheme is used for input and the 3D view
```

### FR-2 Validation

- **FR-2.1** A cube state with the wrong number of stickers of any color is rejected.
- **FR-2.2** On 3x3, the six centers must have six different colors matching the color scheme's opposite pairs.
- **FR-2.3** Every piece must exist exactly once (no impossible color combinations, no duplicates).
- **FR-2.4** The state must be solvable by turning layers (no twisted corner, flipped edge or swapped pair).
- **FR-2.5** On rejection, the app explains the problem in plain language, highlights the suspect stickers and
  suggests what to check (e.g. "a corner may have been twisted by hand").
- **FR-2.6** If the state is already solved, the app says so instead of producing a guide.

```gherkin
Scenario: AC-2.1 Wrong color count
  Given a 3x3 cube state with 10 white stickers
  When the user confirms the cube
  Then the cube is rejected
  And the user is told that white has one sticker too many

Scenario: AC-2.2 Duplicated center color
  Given a 3x3 cube state with two green centers
  When the user confirms the cube
  Then the cube is rejected and both centers are highlighted

Scenario: AC-2.3 Impossible piece
  Given a cube state with a corner colored white, yellow and red
  When the user confirms the cube
  Then the cube is rejected and that corner is highlighted

Scenario: AC-2.4 Twisted corner
  Given a solved 3x3 with one corner twisted in place
  When the user confirms the cube
  Then the cube is rejected
  And the user is told a corner may have been twisted by hand

Scenario: AC-2.5 Plain-language explanation for kids
  Given a Kid profile and a rejected cube state
  Then the explanation uses short sentences and points at the stickers to check

Scenario: AC-2.6 Already solved
  Given a solved cube state
  When the user confirms the cube
  Then the user is told the cube is already solved
```

### FR-3 Solve guide

- **FR-3.1** The user chooses a method among those available for the cube size. The default is Beginner for Kid
  profiles and the last-used method otherwise.
- **FR-3.2** The solution is organized in stages and steps. Each step shows the move animated in 3D, its notation
  and, for stage-based methods, a plain-language explanation. Fast and Optimal show moves only.
- **FR-3.3** Playback controls: play, pause, next step, previous step, jump to a stage, playback speed.
- **FR-3.4** Before the first step, the guide tells the user how to hold the cube (which face up and front).
- **FR-3.5** The guide always shows progress: current stage, step *n* of *m*.
- **FR-3.6** If the user gets lost, they can re-enter the current cube state and the guide continues from it.
- **FR-3.7** Reaching the solved state ends the guide with a celebration and a summary (method, number of moves).

```gherkin
Scenario: AC-3.1 Methods per size
  Given a valid 2x2 cube state
  When the user opens the method choice
  Then the methods offered are Beginner, Ortega and Optimal

Scenario: AC-3.2.1 Explained step
  Given a guide with method Beginner
  When the user is at any step
  Then the move is animated, its notation is shown and an explanation is shown

Scenario: AC-3.2.2 Following the guide solves the cube
  Given any valid cube state and any available method
  When all steps of the solution are applied
  Then the cube is solved

Scenario: AC-3.3 Step back
  Given the user is at step 5
  When the user taps previous step
  Then the move of step 5 is animated in reverse and step 4 becomes current

Scenario: AC-3.4 Holding instruction
  When a guide starts
  Then the user is told which face to hold up and which to hold in front

Scenario: AC-3.5 Progress indicator
  Given a solution with 3 stages and 40 steps
  When the user is at step 12 in stage 2
  Then the guide shows stage 2 and "12 / 40"

Scenario: AC-3.6 Recover when lost
  Given the user is lost at step 20
  When the user re-enters the current cube state
  Then a new solution starts from that state

Scenario: AC-3.7 Completion
  When the last step is applied
  Then a celebration is shown with the method name and the number of moves
```

### FR-4 3D viewer

- **FR-4.1** The cube is shown in 3D and always reflects the current cube state.
- **FR-4.2** The user rotates the whole cube freely by gesture and can reset to the default view.
- **FR-4.3** Each move is animated as a layer turn, with an indicator of the turning direction.
- **FR-4.4** In free-play mode, the user turns layers by gesture.

```gherkin
Scenario: AC-4.1 View reflects state
  Given the cube state changes by one move
  Then the 3D cube shows the new state after the animation

Scenario: AC-4.2 Orbit and reset
  Given the user rotated the cube to see the Back face
  When the user resets the view
  Then the default view is restored and the cube state is unchanged

Scenario: AC-4.3 Direction indicator
  When the move "U'" is played
  Then the Up layer turns counter-clockwise and an arrow shows the direction

Scenario: AC-4.4 Free play
  Given free-play mode on a solved 3x3
  When the user swipes the Right layer upward
  Then the move "R" is applied
```

### FR-5 Tutor

- **FR-5.1** The tutor offers a curriculum per method: ordered lessons with prerequisites. Order of methods:
  notation → Beginner (2x2, 3x3) → Ortega (2x2) → CFOP (from 2-look to full) → Roux.
- **FR-5.2** Each lesson follows: concept → demonstration → guided practice → free practice.
- **FR-5.3** Practice uses cube states prepared so the technique of the lesson is what remains to be done.
- **FR-5.4** Hints decrease as the user improves: full animation → next move → notation only → stage name → none.
  The user can always ask for more help.
- **FR-5.5** A technique is mastered after 5 consecutive successful practices at hint level "none".
- **FR-5.6** A user graduates a method by solving a random scramble on their real cube without hints, confirming
  the result in the app (re-entering the final state or confirming it is solved).
- **FR-5.7** Kid profiles get short sentences, more visuals, positive reinforcement and no timers by default.
- **FR-5.8** The tutor recommends the next lesson based on mastery.
- **FR-5.9** An algorithm reference per method shows each algorithm animated and whether the user has mastered it.

```gherkin
Scenario: AC-5.1 Prerequisites
  Given the user has not mastered notation
  When the user opens the curriculum
  Then the Beginner lessons are shown as locked

Scenario: AC-5.2 Lesson flow
  When the user starts a lesson
  Then the user goes through concept, demonstration, guided practice and free practice in that order

Scenario: AC-5.3 Targeted practice
  Given the lesson "Insert a white corner"
  When a practice starts
  Then only that technique is needed to complete the practice

Scenario: AC-5.4 Hints fade
  Given the user succeeded 3 times at hint level "full animation"
  When the next practice starts
  Then the hint level is "next move"

Scenario: AC-5.5 Mastery
  Given 4 consecutive successes at hint level "none"
  When the user succeeds once more
  Then the technique is marked as mastered

Scenario: AC-5.6 Graduation
  Given all Beginner 3x3 techniques are mastered
  When the user solves a random scramble without hints and confirms it is solved
  Then the user graduates the Beginner 3x3 method and earns its badge

Scenario: AC-5.7 Kid adaptation
  Given a Kid profile
  When a lesson starts
  Then no timer is shown and every success is encouraged

Scenario: AC-5.8 Next lesson
  Given the user mastered "White cross"
  Then the recommended lesson is "Insert a white corner"

Scenario: AC-5.9 Algorithm reference
  When the user opens the CFOP algorithm reference
  Then each algorithm can be played in 3D and shows its mastery status
```

### FR-6 Profiles and progress

- **FR-6.1** Several local profiles, each with a name, an avatar and a kind (Kid or Adult). No personal data is
  required.
- **FR-6.2** Each profile keeps its progress per technique and a history of solves (date, method, moves, time).
- **FR-6.3** An optional timer measures practice solves.
- **FR-6.4** A profile and all its data can be deleted. From a Kid profile, deleting requires an adult check.

```gherkin
Scenario: AC-6.1 Create a profile
  When the user creates a Kid profile named "Sofi"
  Then "Sofi" appears in the profile list with kind Kid

Scenario: AC-6.2 Progress is per profile
  Given profiles "Sofi" and "Ana"
  When "Sofi" masters a technique
  Then the technique is not mastered for "Ana"

Scenario: AC-6.3 Timed solve
  Given the timer is enabled
  When the user finishes a solve
  Then the time is stored in the history

Scenario: AC-6.4 Adult check
  Given the active profile is a Kid profile
  When deleting a profile is requested
  Then an adult check is required before deletion
```

### FR-7 Settings

- **FR-7.1** Language: Spanish or English. Defaults to the device language, or English if unsupported.
- **FR-7.2** Default animation speed.
- **FR-7.3** Color-blind assistance: a distinct symbol on each sticker color.
- **FR-7.4** Handedness: places the main controls for left- or right-handed use.

```gherkin
Scenario: AC-7.1 Default language
  Given the device language is Spanish
  When the app starts for the first time
  Then the app is in Spanish

Scenario: AC-7.2 Default animation speed
  Given the default animation speed is "slow"
  When a guide starts
  Then moves are played at "slow" speed

Scenario: AC-7.3 Color-blind symbols
  Given color-blind assistance is on
  Then every sticker shows the symbol of its color in input, guide and tutor

Scenario: AC-7.4 Left-handed layout
  Given handedness is "left"
  Then the main guide controls are placed on the left side
```

## 6. Quality requirements

- **NFR-1 Offline** — Every feature works without a network connection.
- **NFR-2 Privacy** — No data leaves the device. No accounts, ads, tracking or purchases. Camera images are used only
  to read colors and are never stored.
- **NFR-3 Responsiveness** — A solution appears within 2 seconds. A one-time preparation of up to 30 seconds is
  acceptable for Fast, with visible progress. Animations look smooth on mid-range devices.
- **NFR-4 Correctness** — Every solution solves the cube (100%). Fast solutions have at most 25 moves; Optimal 2x2
  solutions are the shortest possible (at most 11 moves).
- **NFR-5 Accessibility** — Touch targets usable by children; color-blind assistance (FR-7.3); screen-reader
  labels on all controls; text readable by early readers in Kid profiles.
- **NFR-6 Platforms** — Android 8.0+ and iOS 16+, phones and tablets.
- **NFR-7 Localization** — All text available in Spanish and English.
- **NFR-8 Child safety** — No external links, chat or user-generated content reachable from a Kid profile.

## 7. Out of scope (v1)

Cubes other than 2x2 and 3x3; accounts or cloud sync; online or competitive modes; Bluetooth smart cubes;
blindfolded methods; audio narration.
