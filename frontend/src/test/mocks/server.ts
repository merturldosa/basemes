/**
 * MSW Server Setup for Node Environment (Tests)
 * @author Moon Myung-seop
 */

import { setupServer } from 'msw/node';
import { handlers } from './handlers';

// Setup MSW server with handlers
export const server = setupServer(...handlers);
