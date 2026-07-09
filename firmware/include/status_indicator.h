#pragma once

namespace leds {
void setActiveIndicator(bool state);
void setErrorIndicator(bool state);
void setWriteIndicator(bool state);

void indicateConnected();
}  // namespace leds
