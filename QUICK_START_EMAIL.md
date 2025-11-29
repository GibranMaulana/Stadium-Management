# Quick Start Guide - Email Service

## 🚀 Get Started in 5 Minutes

### Step 1: Get Gmail App Password (2 minutes)

1. Open: https://myaccount.google.com/apppasswords
2. Sign in to your Google Account
3. Select: **Mail** → **Other (Custom name)** → Type "Stadium"
4. Click **Generate**
5. Copy the 16-character password: `xxxx xxxx xxxx xxxx`

### Step 2: Update Configuration (1 minute)

Open `.env` file and update:

```env
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=xxxx xxxx xxxx xxxx
EMAIL_FROM=noreply@stadiummanagement.com
```

### Step 3: Install Dependencies (1 minute)

```bash
cd /home/bammm/coding/mulet-stadium
mvn clean install
```

### Step 4: Test (1 minute)

```bash
# Run the test
cd src/main/java
java org.openjfx.util.TestEmailService

# Or if that doesn't work:
cd /home/bammm/coding/mulet-stadium
mvn exec:java -Dexec.mainClass="org.openjfx.util.TestEmailService"
```

You should see:
```
✓ Email configuration is valid!
✓ Booking confirmation email sent successfully!
✓ Booking cancellation email sent successfully!
🎉 All tests PASSED!
```

### Step 5: Done! 🎉

Your booking system will now automatically send emails when:
- ✅ Customer creates a booking → Confirmation email sent
- ❌ Admin cancels a booking → Cancellation email sent

---

## 📧 What Emails Look Like

**Confirmation Email:**
- Subject: "Booking Confirmation - [Event Name]"
- Contains: Booking number, event details, seat info, price
- Beautiful HTML template with colors and emojis

**Cancellation Email:**
- Subject: "Booking Cancellation - [Event Name]"
- Contains: Cancelled booking details, contact info

---

## ❓ Troubleshooting

### "Authentication failed"
→ Make sure you're using **App Password**, not your regular Gmail password

### "Connection timeout"
→ Check your internet connection and firewall settings

### "Package javax.mail not accessible"
→ Run: `mvn clean install` and reload project

### Still not working?
→ Read `EMAIL_SETUP.md` for detailed troubleshooting

---

## 📝 For Your Coworker

Send them:
1. This file (QUICK_START.md)
2. The `.env` file (or have them create their own App Password)
3. Tell them to run: `mvn clean install`

They do NOT need to modify any code - it's all ready!

---

## 🔍 Check Logs

When booking is created, you'll see in console:
```
✓ Booking confirmation email sent successfully for booking: BK-20251129-0001
✓ Booking confirmation email sent to: customer@example.com
```

If email fails:
```
✗ Failed to send booking confirmation email for booking: BK-20251129-0001
Authentication failed: ...
```

---

## 📚 Full Documentation

- **Complete Setup:** `EMAIL_SETUP.md`
- **Implementation Details:** `EMAIL_IMPLEMENTATION_SUMMARY.md`

---

**Need Help?** Check the console output for error messages!
