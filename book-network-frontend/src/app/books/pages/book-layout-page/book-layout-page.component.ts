import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { MenuComponent } from '../../components/menu/menu.component';

@Component({
  selector: 'app-book-layout-page',
  standalone: true,
  imports: [RouterOutlet, MenuComponent],
  templateUrl: './book-layout-page.component.html',
  styles: ``
})
export class BookLayoutPageComponent {

}
