import { expect, test } from '@playwright/test'

test.describe('register', () => {
  test('shows confirmation message after successful registration', async ({ page }) => {
    await page.route('**/api/auth/register', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          token: 'fake-token',
          email: 'test@example.local',
          displayName: 'Testuser',
          role: 'USER'
        })
      })
    })

    await page.goto('/register')

    await page.locator('#username').fill('Testuser')
    await page.locator('#email').fill('Test@example.local')
    await page.locator('#password').fill('Testpasswort')
    await page.locator('input[type="checkbox"][required]').check()
    await page.getByRole('button', { name: 'Registrieren' }).click()

    await expect(page.getByText('Registrierung erfolgreich')).toBeVisible()
  })
})
