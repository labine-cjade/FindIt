FUNCTIONS 
- SEARCH BAR
- HERO PAGE CLICK
- DROPDOWN LOCATIONS
- VOICE RECORD FOR SEARCH BUTTON
- ADD BUTTON FOR PICTURE FOR FLOOR 1, 2, 3


## Jetpack Compose conversion (Dashboard, See Details, Update, Add)

Converted from XML to Compose:
- `MainActivity.kt` + `ui/home/HomeScreen.kt` : Dashboard (top bar, hero carousel, floor dropdown, item grid, bottom buttons)
- `ui/components/ItemCardSheet.kt` : one bottom sheet for Add, See Details and Update
- `ui/components/CommonViews.kt` : item card, round buttons, text field
- `ui/components/VoiceInput.kt` : mic for Location (speech to text)
- `ui/components/VoiceMessageRecorder.kt` : record and play the voice message
- `ui/components/PhotoImage.kt` : shows the saved photo
- `TakePictureActivity.kt` : camera, floor tabs, then the Add Item sheet

Flow:
- See Details -> Details sheet (photo, name, location, playable recorded message) -> Update -> editable form -> Update Item -> "Do you want to save changes?" -> Yes
- Add: camera -> shutter -> Add Item sheet -> Save

Mic buttons:
- Location mic: opens the phone's speech input and puts the spoken text in Location.
- Record Message: mic button starts recording (turns into a red stop button), tap again to stop, play button plays it.
  Needs the RECORD_AUDIO permission (asked on first use). The file is saved with the item and can be played in See Details.

Not changed (still XML): Search, Login, Sign Up, Onboarding. Items are kept in memory (InventoryManager), so they reset when the app is closed.
