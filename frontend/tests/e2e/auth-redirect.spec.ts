import { expect, test } from '@playwright/test'

test.describe('auth redirect', () => {
  test('redirects anonymous users from dashboard to login', async ({ page }) => {
    await page.goto('/')

    await expect(page).toHaveURL(/\/login/)
    await expect(page.getByRole('heading', { name: 'Anmeldung' })).toBeVisible()
  })
})
