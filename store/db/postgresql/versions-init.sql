-- SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO zimbra.config (name, value, description) VALUES
  ('db.version', '111', 'db schema version'),
  ('index.version', '2', 'index version'),
  ('redolog.version', '1.42', 'redolog version');
